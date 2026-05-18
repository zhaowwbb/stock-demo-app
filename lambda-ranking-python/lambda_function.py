from datetime import datetime, timedelta
import os
import pg8000.native
import yfinance as yf

# Database Connection config loaded from Environment Variables
DB_HOST = os.environ.get("DB_HOST", "localhost")
DB_NAME = os.environ.get("DB_NAME", "zafin")
DB_USER = os.environ.get("DB_USER", "dm")
DB_PASSWORD = os.environ.get("DB_PASSWORD", "dm")


# =====================================================================
# (1) FETCH UNIQUE SYMBOLS FROM STOCK_HISTORY
# =====================================================================
def get_unique_symbols_from_db():
    """Queries stock_history to discover all unique stock symbols present."""
    print("Executing: Fetching unique symbols from database...")
    try:
        con = pg8000.native.Connection(
            host=DB_HOST, database=DB_NAME, user=DB_USER, password=DB_PASSWORD
        )

        query = "SELECT DISTINCT symbol FROM stock_history WHERE symbol IS NOT NULL;"
        rows = con.run(query)
        con.close()

        # pg8000 returns rows as lists of values, e.g., [['AAPL'], ['MSFT']]
        symbol_list = [row[0] for row in rows]
        print(f"Database query complete. Found symbols: {symbol_list}")
        return symbol_list

    except Exception as e:
        print(f"DATABASE ERROR in get_unique_symbols_from_db: {e}")
        return []


# =====================================================================
# (2) PROCESS STOCK DATA AND UPSERT BACK TO DB
# =====================================================================
def save_to_database(records):
    """Saves daily pricing records back into stock_history using an UPSERT query."""
    if not records:
        return

    try:
        con = pg8000.native.Connection(
            host=DB_HOST, database=DB_NAME, user=DB_USER, password=DB_PASSWORD
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

        for r in records:
            con.run(
                upsert_query,
                symbol=r["symbol"],
                updated_date=r["updated_date"],
                current_price=r["current_price"],
                open_price=r["open_price"],
                high_price=r["high_price"],
                low_price=r["low_price"],
                volume=r["volume"],
            )

        con.close()
        print(f"Successfully upserted {len(records)} records to stock_history.")
    except Exception as e:
        print(f"DATABASE ERROR in save_to_database: {e}")


def process_stock(stock_symbol_list, target_date_str):
    """Downloads prices from yfinance for target_date_str, prints logs,

    and triggers database synchronization.
    """
    # Print requirement: total number of symbols and date
    print(
        f"Executing process_stock: Total Symbols = {len(stock_symbol_list)} | Processing Target Date = {target_date_str}"
    )

    if not stock_symbol_list:
        print("Symbol list is empty. Skipping process_stock execution.")
        return

    # Establish the single day download criteria boundaries
    start_dt = datetime.strptime(target_date_str, "%Y-%m-%d")
    end_date_str = (start_dt + timedelta(days=1)).strftime("%Y-%m-%d")

    try:
        data = yf.download(
            stock_symbol_list,
            start=target_date_str,
            end=end_date_str,
            progress=False,
            group_by="ticker",
        )
    except Exception as e:
        print(f"API Fetch Error inside process_stock for {target_date_str}: {e}")
        return

    calculated_results = []

    for symbol in stock_symbol_list:
        if symbol not in data or data[symbol].empty:
            continue

        day_data = data[symbol].dropna()
        if day_data.empty:
            continue

        # Extract parameters out of data columns safely
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
            "volume": volume,
        }
        calculated_results.append(result_record)

    # Trigger saving logic
    save_to_database(calculated_results)


# =====================================================================
# (3) CALCULATE TOP 10 ABSOLUTE HIGH PRICE MINUS EARLIEST LOW PRICE
# =====================================================================
def calculate_top_absolute_increase(today_str):
    """Calculates Max(high_price) minus Min(low_price) per ticker over

    all data history, runs data cleanup, and saves to top_rec_absolute_increase.
    """
    print(
        "Executing: Scanning history table to calculate Top 10 Absolute Price Increases..."
    )
    try:
        con = pg8000.native.Connection(
            host=DB_HOST, database=DB_NAME, user=DB_USER, password=DB_PASSWORD
        )

        # Step 3 Data Cleanup requirement: Clear out old rankings for this calculation day
        con.run(
            "DELETE FROM top_rec_absolute_increase WHERE updated_date = :dt;",
            dt=today_str,
        )

        # SQL Aggregate to find: Max High, Min Low, Last known Volume, and the dollar spread
        calc_query = """
            SELECT 
                symbol,
                MAX(high_price) as max_high,
                MIN(low_price) as min_low,
                (SELECT volume FROM stock_history sh2 WHERE sh2.symbol = sh1.symbol ORDER BY updated_date DESC LIMIT 1) as current_vol,
                (MAX(high_price) - MIN(low_price)) as absolute_diff
            FROM stock_history sh1
            GROUP BY symbol
            HAVING MAX(high_price) IS NOT NULL AND MIN(low_price) IS NOT NULL
            ORDER BY absolute_diff DESC
            LIMIT 10;
        """
        top_rows = con.run(calc_query)

        insert_query = """
            INSERT INTO top_rec_absolute_increase (
                rank, updated_date, symbol, price_high, price_low, volume, price_increase_amt
            ) VALUES (:rank, :updated_date, :symbol, :price_high, :price_low, :volume, :price_increase_amt);
        """

        for idx, row in enumerate(top_rows):
            con.run(
                insert_query,
                rank=idx + 1,
                updated_date=today_str,
                symbol=str(row[0]),
                price_high=float(row[1]),
                price_low=float(row[2]),
                volume=int(row[3]) if row[3] is not None else 0,
                price_increase_amt=float(row[4]),
            )

        con.close()
        print("Calculation Complete: Saved Top 10 Absolute Increase recommendations.")

    except Exception as e:
        print(f"DATABASE ERROR in calculate_top_absolute_increase: {e}")


# =====================================================================
# (4) CALCULATE TOP 10 PERCENTAGE INCREASE
# =====================================================================
def calculate_top_percentage_increase(today_str):
    """Calculates percentage increase over data history, runs cleanup,

    and updates top_rec_percentage_increase.
    """
    print(
        "Executing: Scanning history table to calculate Top 10 Percentage Price Growth..."
    )
    try:
        con = pg8000.native.Connection(
            host=DB_HOST, database=DB_NAME, user=DB_USER, password=DB_PASSWORD
        )

        # Step 4 Data Cleanup requirement: Clear out old rankings for this calculation day
        con.run(
            "DELETE FROM top_rec_percentage_increase WHERE updated_date = :dt;",
            dt=today_str,
        )

        # SQL Formula: ((Max High - Min Low) / Min Low) * 100
        calc_query = """
            SELECT 
                symbol,
                MAX(high_price) as max_high,
                MIN(low_price) as min_low,
                (SELECT volume FROM stock_history sh2 WHERE sh2.symbol = sh1.symbol ORDER BY updated_date DESC LIMIT 1) as current_vol,
                (((MAX(high_price) - MIN(low_price)) / NULLIF(MIN(low_price), 0)) * 100) as percentage_diff
            FROM stock_history sh1
            GROUP BY symbol
            HAVING MIN(low_price) > 0
            ORDER BY percentage_diff DESC
            LIMIT 10;
        """
        top_rows = con.run(calc_query)

        insert_query = """
            INSERT INTO top_rec_percentage_increase (
                rank, updated_date, symbol, price_high, price_low, volume, price_increase_pct
            ) VALUES (:rank, :updated_date, :symbol, :price_high, :price_low, :volume, :price_increase_pct);
        """

        for idx, row in enumerate(top_rows):
            con.run(
                insert_query,
                rank=idx + 1,
                updated_date=today_str,
                symbol=str(row[0]),
                price_high=float(row[1]),
                price_low=float(row[2]),
                volume=int(row[3]) if row[3] is not None else 0,
                price_increase_pct=float(row[4]),
            )

        con.close()
        print(
            "Calculation Complete: Saved Top 10 Percentage Increase recommendations."
        )

    except Exception as e:
        print(f"DATABASE ERROR in calculate_top_percentage_increase: {e}")


# =====================================================================
# (5) AWS LAMBDA HANDLER CORE ENGINE ENTRY POINT
# =====================================================================
def lambda_handler(event, context):
    """Main execution container for AWS Lambda orchestration."""
    print("=== AWS LAMBDA PIPELINE TRIGGERED ===")

    # Get string representation for today
    today_str = datetime.today().strftime("%Y-%m-%d")

    # Step 1: Scan table to discover unique symbols
    symbols_to_track = get_unique_symbols_from_db()

    if not symbols_to_track:
        print("Execution halted: No unique symbols discovered in stock_history.")
        return {
            "statusCode": 200,
            "body": "Pipeline completed with no active tickers to scan.",
        }

    # Step 2: Download daily data and update values
    process_stock(symbols_to_track, today_str)

    # Step 3: Parse history metrics for absolute growth index tracking
    calculate_top_absolute_increase(today_str)

    # Step 4: Parse history metrics for percentage growth index tracking
    calculate_top_percentage_increase(today_str)

    # Step 5: Wrap up printing confirmation log information
    print(
        f"=== PROCESSING COMPLETE: Pipeline execution for date {today_str} finalized successfully. ==="
    )

    return {
        "statusCode": 200,
        "body": f"Successfully updated metrics and computed recommendations lists for {today_str}.",
    }