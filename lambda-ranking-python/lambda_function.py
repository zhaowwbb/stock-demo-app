import os
import pg8000.native
import pandas as pd
import yfinance as yf

# Database Connection configuration from environment variables
DB_HOST = os.environ.get("DB_HOST", "localhost")
DB_NAME = os.environ.get("DB_NAME", "zafin")
DB_USER = os.environ.get("DB_USER", "dm")
DB_PASSWORD = os.environ.get("DB_PASSWORD", "dm")

# List of target tickers to screen (Expand this as needed for your demo)
TICKERS = ["AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "NVDA", "META", "NFLX", "AMD", "INTC", "BABA", "V", "DIS"]

def lambda_handler(event, context):
    print("Starting stock data calculation...")
    
    # 1. Fetch historical data for the last 5 days (to safely capture 3 trading days)
    data = yf.download(TICKERS, period="5d", interval="1d")
    
    # Extract adjusted close prices
    close_prices = data['Close']
    
    # Ensure we take the last 3 available trading days
    if len(close_prices) < 3:
        print("Not enough market days available.")
        return {"statusCode": 400, "body": "Insufficient data"}
        
    recent_3_days = close_prices.tail(3)
    
    # 2. Calculate the 3-day change percentage: ((Day 3 - Day 1) / Day 1) * 100
    price_start = recent_3_days.iloc[0]
    price_end = recent_3_days.iloc[-1]
    pct_change = ((price_end - price_start) / price_start) * 100
    
    # Build results dataframe
    results_df = pd.DataFrame({
        'current_price': price_end,
        'price_change_pct': pct_change
    }).dropna()
    
    # Sort and slice the top 10
    top_10 = results_df.sort_values(by="price_change_pct", ascending=False).head(10).reset_index()
    top_10.columns = ['symbol', 'current_price', 'price_change_pct']
    
    print("Calculated Top 10 Stocks:")
    print(top_10)
    
    # 3. Save results to RDS PostgreSQL
    try:
        con = pg8000.native.Connection(
            host=DB_HOST,
            database=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD
        )
        
        # Ensure table exists
        con.run("""
            CREATE TABLE IF NOT EXISTS top_recommendations (
                rank INT PRIMARY KEY,
                symbol VARCHAR(10),
                price NUMERIC(10, 2),
                change_3d_pct NUMERIC(10, 2),
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """)
        
        # Clear old recommendations for the demo update
        con.run("TRUNCATE TABLE top_recommendations;")
        
        # Insert current top 10
        for index, row in top_10.iterrows():
            con.run(
                """
                INSERT INTO top_recommendations (rank, symbol, price, change_3d_pct) 
                VALUES (:rank, :symbol, :price, :change_3d_pct);
                """,
                rank=int(index + 1),
                symbol=str(row['symbol']),
                price=float(row['current_price']),
                change_3d_pct=float(row['price_change_pct'])
            )
            
        con.close()
        print("Successfully updated database.")
        return {"statusCode": 200, "body": "Top 10 Stocks Updated Successfully!"}
        
    except Exception as e:
        print(f"Database error: {str(e)}")
        return {"statusCode": 500, "body": f"Error saving to database: {str(e)}"}
    
 