package main

import (
    "bytes"
    "context"
    "database/sql"
    "encoding/json"
    "log"
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

func connectDB() (*sql.DB, error) {
    conn := "host=localhost port=5432 user=postgres password=password dbname=stocks sslmode=disable"
    return sql.Open("postgres", conn)
}

func handler(ctx context.Context) error {

    db, err := connectDB()
    if err != nil {
        return err
    }

    rows, err := db.Query(`
        SELECT symbol, MIN(price), MAX(price)
        FROM stocks
        GROUP BY symbol
    `)

    if err != nil {
        return err
    }

    results := []StockResult{}

    for rows.Next() {

        var symbol string
        var minPrice float64
        var maxPrice float64

        rows.Scan(&symbol, &minPrice, &maxPrice)

        results = append(results, StockResult{
            Symbol: symbol,
            PriceIncrease: maxPrice - minPrice,
            LatestPrice: maxPrice,
        })
    }

    sort.Slice(results, func(i, j int) bool {
        return results[i].PriceIncrease > results[j].PriceIncrease
    })

    if len(results) > 10 {
        results = results[:10]
    }

    for i := range results {
        results[i].Rank = i + 1

        _, err := db.Exec(`
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
            return err
        }
    }

    payload := map[string]interface{}{
        "generatedAt": time.Now(),
        "top10": results,
    }

    jsonData, _ := json.MarshalIndent(payload, "", "  ")

    cfg, err := config.LoadDefaultConfig(context.TODO())
    if err != nil {
        return err
    }

    client := s3.NewFromConfig(cfg)

    _, err = client.PutObject(context.TODO(), &s3.PutObjectInput{
        Bucket: aws.String("your-stock-demo-bucket"),
        Key: aws.String("top10.json"),
        Body: bytes.NewReader(jsonData),
        ContentType: aws.String("application/json"),
    })

    if err != nil {
        return err
    }

    log.Println("Uploaded top10.json successfully")

    return nil
}

func main() {
    lambda.Start(handler)
}
