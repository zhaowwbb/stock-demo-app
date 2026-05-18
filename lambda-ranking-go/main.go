package main

import (
	"bytes"
	"context"
	"database/sql"
	"encoding/json"
	"fmt"
	"log"
	"os"
	"sort"
	"time"

	"github.com/aws/aws-lambda-go/lambda"
	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/service/s3"
	_ "github.com/lib/pq"
)

type StockResult struct {
	Symbol        string  `json:"symbol"`
	PriceIncrease float64 `json:"priceIncrease"`
	LatestPrice   float64 `json:"latestPrice"`
	Rank          int     `json:"rank"`
}

type PriceRecord struct {
	MinPrice float64
	MaxPrice float64
}

// connectDB reads connection variables from the environment
func connectDB() (*sql.DB, error) {
	host := os.Getenv("DB_HOST")
	port := os.Getenv("DB_PORT")
	user := os.Getenv("DB_USERNAME")
	password := os.Getenv("DB_PASSWORD")
	dbname := os.Getenv("DB_NAME")

	// Fallback to localhost defaults if environment variables aren't set (for local testing)
	if host == "" {
		host = "localhost"
	}
	if port == "" {
		port = "5432"
	}
	if user == "" {
		user = "dm"
	}
	if password == "" {
		password = "dm"
	}
	if dbname == "" {
		dbname = "zafin"
	}

	connStr := fmt.Sprintf("host=%s port=%s user=%s password=%s dbname=%s sslmode=require",
		host, port, user, password, dbname)

	// Note: If testing locally against localhost without SSL, change sslmode to 'disable'
	// or make sslmode its own environment variable. For RDS, 'require' is highly recommended.

	return sql.Open("postgres", connStr)
}

func handler(ctx context.Context) error {
	db, err := connectDB()
	if err != nil {
		return fmt.Errorf("failed to open database: %w", err)
	}
	defer db.Close()

	rows, err := db.QueryContext(ctx, `
		SELECT symbol, MIN(price), MAX(price)
		FROM stocks
		GROUP BY symbol
	`)
	if err != nil {
		return fmt.Errorf("failed to query stocks: %w", err)
	}
	defer rows.Close()

	var results []StockResult

	for rows.Next() {
		var symbol string
		var minPrice float64
		var maxPrice float64

		if err := rows.Scan(&symbol, &minPrice, &maxPrice); err != nil {
			return fmt.Errorf("failed to scan row: %w", err)
		}
		fmt.Println("symbol:", symbol, "minPrice:", minPrice, "maxPrice:", maxPrice)

		results = append(results, StockResult{
			Symbol:        symbol,
			PriceIncrease: maxPrice - minPrice,
			LatestPrice:   maxPrice,
		})
	}

	// Check for errors encountered during iteration
	if err = rows.Err(); err != nil {
		return fmt.Errorf("error during rows iteration: %w", err)
	}

	sort.Slice(results, func(i, j int) bool {
		return results[i].PriceIncrease > results[j].PriceIncrease
	})

	if len(results) > 10 {
		results = results[:10]
	}

	for i := range results {
		results[i].Rank = i + 1

		_, err := db.ExecContext(ctx, `
			INSERT INTO stock_ranking
			(symbol, ranking, score, calculate_time)
			VALUES ($1, $2, $3, $4)
		`,
			results[i].Symbol,
			results[i].Rank,
			results[i].PriceIncrease,
			time.Now(),
		)
		if err != nil {
			return fmt.Errorf("failed to insert ranking for %s: %w", results[i].Symbol, err)
		}
	}

	payload := map[string]interface{}{
		"generatedAt": time.Now(),
		"top10":       results,
	}

	jsonData, err := json.MarshalIndent(payload, "", "  ")
	if err != nil {
		return fmt.Errorf("failed to marshal payload: %w", err)
	}

	// AWS_REGION env var is automatically consumed by config.LoadDefaultConfig
	cfg, err := config.LoadDefaultConfig(ctx)
	if err != nil {
		return fmt.Errorf("failed to load AWS configuration: %w", err)
	}

	bucketName := os.Getenv("S3_BUCKET")
	if bucketName == "" {
		bucketName = "demo-stock-ranking-bucket-2026" // Default fallback
	}

	client := s3.NewFromConfig(cfg)

	_, err = client.PutObject(ctx, &s3.PutObjectInput{
		Bucket:      aws.String(bucketName),
		Key:         aws.String("top10.json"),
		Body:        bytes.NewReader(jsonData),
		ContentType: aws.String("application/json"),
	})
	if err != nil {
		return fmt.Errorf("failed to upload to S3 bucket %s: %w", bucketName, err)
	}

	log.Printf("Uploaded top10.json successfully to %s", bucketName)
	return nil
}

func main() {
	// If executing inside the AWS Lambda environment, the AWS_LAMBDA_FUNCTION_NAME
	// env variable is automatically populated by AWS.
	if os.Getenv("AWS_LAMBDA_FUNCTION_NAME") != "" {
		lambda.Start(handler)
	} else {
		// Local manual execution block
		log.Println("Running execution test locally...")
		ctx := context.Background()
		if err := handler(ctx); err != nil {
			log.Fatalf("Handler returned an error: %v", err)
		}
		log.Println("Complete local go lambda test successfully")
	}
}
