from datetime import datetime, timedelta
import os
import time
import pandas as pd
import pg8000.native
from pytickersymbols import PyTickerSymbols
import yfinance as yf

# Updated Database Connection configuration with your local parameters
DB_HOST = os.environ.get("DB_HOST", "localhost")
DB_NAME = os.environ.get("DB_NAME", "zafin")
DB_USER = os.environ.get("DB_USER", "dm")
DB_PASSWORD = os.environ.get("DB_PASSWORD", "dm")


def get_popular_tickers():
    """Returns a list of at least 50 of the most popular and actively traded

    US stock symbols across various major sectors.
    """
    return [
        "AAPL", "MSFT", "GOOGL", "AMZN", "NVDA", "TSLA", "META", "NFLX", "AMD", "INTC",
        "AVGO", "QCOM", "CRM", "ADBE", "ORAC", "CSCO", "SMCI", "PLTR", "PANW", "UBER",
        "BRK-B", "JPM", "BAC", "WFC", "MS", "GS", "V", "MA", "AXP", "PYPL",
        "WMT", "HD", "COST", "TGT", "NKE", "SBUX", "KO", "PEP", "DIS", "CMG",
        "LLY", "UNH", "JNJ", "PFE", "MRK", "ABBV", "ISRG", "XOM", "CVX", "CAT",
        "GE", "F", "GM"
    ]


def process_stock(stock_symbol_list, target_date_str):
    """Loops through symbols, queries yfinance for the specific date,

    prints each result_record, and returns a list of calculated results.
    """
    print(f"\n--- Processing Stocks for Date: {target_date_str} ---")

    start_dt = datetime.strptime(target_date_str, "%Y-%m-%d")
    end_dt = start_dt + timedelta(days=1)
    end_date_str = end_dt.strftime("%Y-%m-%d")

    calculated_results = []

    try:
        data = yf.download(
            stock_symbol_list,
            start=target_date_str,
            end=end_date_str,
            progress=False,
            group_by="ticker",
        )
    except Exception as e:
        print(f"Error downloading data batch for {target_date_str}: {e}")
        return []

    for symbol in stock_symbol_list:
        if symbol not in data or data[symbol].empty:
            print(f"WARNING LOG: No data available for {symbol} on {target_date_str}. Excluding.")
            continue

        day_data = data[symbol].dropna()
        if day_data.empty:
            print(f"WARNING LOG: Empty data row for {symbol} on {target_date_str}. Excluding.")
            continue

        # Extract the OHLCV fields safely
        current_price = float(day_data["Close"].iloc[0])
        open_price = float(day_data["Open"].iloc[0])
        high_price = float(day_data["High"].iloc[0])
        low_price = float(day_data["Low"].iloc[0])
        volume = int(day_data["Volume"].iloc[0])

        result_record = {
            "symbol": symbol,
            "updated_date": target_date_str,
            "current_price": round(current_price, 4),
            "open_price": round(open_price, 4),
            "high_price": round(high_price, 4),
            "low_price": round(low_price, 4),
            "volume": volume
        }
        
        # Print the individual result record as requested
        print(f"Result Record: {result_record}")
        
        calculated_results.append(result_record)

    return calculated_results


def save_to_database(records):
    """Connects to PostgreSQL and performs an UPSERT on the stock_history table."""
    if not records:
        print("No records available to save to the database.")
        return

    print(f"\nConnecting to database to save {len(records)} records...")
    try:
        con = pg8000.native.Connection(
            host=DB_HOST,
            database=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD
        )

        upsert_query = """
            INSERT INTO stock_history (
                symbol, updated_date, current_price, open_price, high_price, low_price, volume
            ) VALUES (
                :symbol, :updated_date, :current_price, :open_price, :high_price, :low_price, :volume
            )
            ON CONFLICT (symbol, updated_date) 
            DO UPDATE SET 
                current_price = EXCLUDED.current_price,
                open_price = EXCLUDED.open_price,
                high_price = EXCLUDED.high_price,
                low_price = EXCLUDED.low_price,
                volume = EXCLUDED.volume,
                created_at = CURRENT_TIMESTAMP;
        """

        for record in records:
            con.run(
                upsert_query,
                symbol=record["symbol"],
                updated_date=record["updated_date"],
                current_price=record["current_price"],
                open_price=record["open_price"],
                high_price=record["high_price"],
                low_price=record["low_price"],
                volume=record["volume"]
            )

        con.close()
        print("Successfully synchronized all records with PostgreSQL (UPSERT completed).")

    except Exception as e:
        print(f"DATABASE ERROR: Failed to write records to PostgreSQL. Details: {e}")


def calculate_price_history():
    """Gathers symbols from popular tickers exclusively, establishes a trailing 7-day

    date window, processes data with a 30-second delay per day, and updates the database.
    """
    print("\n--- Initializing Price History Calculation ---")
    
    # Updated: Now loading symbols strictly from get_popular_tickers
    symbols = get_popular_tickers()
    print(f"Loaded {len(symbols)} popular symbols to process.")

    # Build the 7-day target calculation array (inclusive of today)
    today = datetime.today()
    date_list = []
    for i in range(7):
        day = today - timedelta(days=i)
        date_list.append(day.strftime("%Y-%m-%d"))

    date_list.reverse()
    print(f"Date list to process (last 7 days): {date_list}")

    all_history_records = []
    
    # Loop through the dates to pull information
    for index, date_str in enumerate(date_list):
        day_results = process_stock(symbols, date_str)
        all_history_records.extend(day_results)
        
        # Updated: Add 30 seconds delay for each date_str processing (except the last item)
        if index < len(date_list) - 1:
            print(f"Sleeping for 30 seconds to prevent API throttling before starting next date...")
            time.sleep(30)

    # Trigger database persistence function
    save_to_database(all_history_records)

    return all_history_records


def main():
    """Main execution function block."""
    print("=== STOCK COLLECTION TOOL STARTED ===")
    calculate_price_history()
    print("\n=== STOCK COLLECTION TOOL FINISHED SUCCESSFULLY ===")


if __name__ == "__main__":
    main()