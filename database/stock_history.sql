-- Create the stock_history table
CREATE TABLE IF NOT EXISTS stock_history (
    symbol VARCHAR(10) NOT NULL,
    updated_date DATE NOT NULL,
    current_price NUMERIC(12, 4) NOT NULL,
    open_price NUMERIC(12, 4),
    high_price NUMERIC(12, 4),
    low_price NUMERIC(12, 4),
    volume BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Composite Primary Key: Prevents duplicate records for the same ticker on the same day
    PRIMARY KEY (symbol, updated_date)
);

-- Index for optimized querying by date range (useful for your 3-day or 7-day calculations)
CREATE INDEX IF NOT EXISTS idx_stock_history_date 
ON stock_history (updated_date DESC);

-- Index for optimized querying by a specific stock ticker symbol
CREATE INDEX IF NOT EXISTS idx_stock_history_symbol 
ON stock_history (symbol);