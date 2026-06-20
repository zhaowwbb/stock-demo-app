CREATE TABLE stock_price (
    id SERIAL PRIMARY KEY,
    symbol VARCHAR(20),
    open NUMERIC,
    high NUMERIC,
    low NUMERIC,
    close NUMERIC,
    volume BIGINT,
    updated_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);