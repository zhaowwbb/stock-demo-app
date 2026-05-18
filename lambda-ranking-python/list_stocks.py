import time
import financedatabase as fd
import pandas as pd
import yfinance as yf
from pytickersymbols import PyTickerSymbols

from pytickersymbols import PyTickerSymbols


def get_all_us_stock_symbols():
    """Fetches unique stock symbols from S&P 500 and NASDAQ 100 indices

    safely extracting the Yahoo Finance formatted ticker string.
    """
    print("Fetching major US stock symbols...")
    stock_data = PyTickerSymbols()

    # 1. Fetch the stock iterators
    sp500_iterator = stock_data.get_stocks_by_index("S&P 500")
    nasdaq100_iterator = stock_data.get_stocks_by_index("NASDAQ 100")

    unique_tickers = set()

    # Helper function to parse symbols safely out of the complex dict structure
    def extract_symbols(iterator):
        for stock in iterator:
            if "symbols" in stock and stock["symbols"]:
                # The first item inside symbols is a dict containing specific exchange keys
                symbol_entry = stock["symbols"][0]

                if isinstance(symbol_entry, dict):
                    # Use the "yahoo" key since we are passing these to yfinance
                    ticker = symbol_entry.get("yahoo")
                    if ticker:
                        unique_tickers.add(ticker)
                elif isinstance(symbol_entry, str):
                    # Fallback case if some entries are raw strings
                    unique_tickers.add(symbol_entry)

    # 2. Process both indices
    extract_symbols(sp500_iterator)
    extract_symbols(nasdaq100_iterator)

    # 3. Convert back to a sorted list
    return sorted(list(unique_tickers))

def process_stock_batches(tickers, batch_size=100):
    """Downloads stock data in chunks and extracts the maximum 5-day high price

    for each valid ticker.
    """
    all_high_prices = {}
    print(f"\nStarting data retrieval in batches of {batch_size}...")

    # Loop through tickers in chunks of 'batch_size' (e.g., 100)
    for i in range(0, len(tickers), batch_size):
        batch = tickers[i : i + batch_size]
        print(
            f"Processing batch {i//batch_size + 1}: Tickers {i} to {i + len(batch)}..."
        )

        try:
            # Single API call for the entire 100-stock batch
            data = yf.download(
                batch, period="5d", interval="1d", progress=False, group_by="ticker"
            )

            # Extract the specific "High" pricing metrics from the batch DataFrame
            for ticker in batch:
                if ticker not in data or data[ticker].empty:
                    continue

                if "High" in data[ticker]:
                    max_high = data[ticker]["High"].max()

                    # Prevent bad/corrupted data inputs (like NaN values)
                    if pd.notna(max_high):
                        all_high_prices[ticker] = float(max_high)

        except Exception as e:
            print(f"Error processing batch starting at index {i}: {e}")

        # Polite 1-second delay to protect against Yahoo Finance rate limits
        time.sleep(1)

    return all_high_prices


def calculate_top_10_highest(high_prices_dict):
    """Sorts the compiled high prices and returns a formatted Top 10 DataFrame."""
    # Sort descending by price value
    sorted_stocks = sorted(
        high_prices_dict.items(), key=lambda item: item[1], reverse=True
    )
    top_10_highest = sorted_stocks[:10]

    # Convert to standard DataFrame
    return pd.DataFrame(
        top_10_highest, columns=["Stock Symbol", "Recent 5-Day High Price ($)"]
    )


def main():
    # 1. Fetch available stock symbols
    all_tickers = get_all_us_stock_symbols()
    print(f"Total available US Stock symbols found: {len(all_tickers)}")

    # For safety/demo testing purposes, we cap the run to the first 500 stocks.
    # Comment out this slicing line if you want to scan all thousands of symbols.
    test_tickers = all_tickers[:500]
    print(f"Processing a subset of {len(test_tickers)} stocks for this run...")

    # 2. Run isolated batch processing logic
    high_prices_data = process_stock_batches(test_tickers, batch_size=100)

    # 3. Calculate and display the final top 10
    df_top_10 = calculate_top_10_highest(high_prices_data)

    print("\n" + "=" * 45)
    print("   TOP 10 STOCKS WITH THE HIGHEST PRICE")
    print("=" * 45)
    print(df_top_10.to_string(index=False))


if __name__ == "__main__":
    main()