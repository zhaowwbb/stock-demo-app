DROP TABLE IF EXISTS stock_ranking;
DROP TABLE IF EXISTS stocks;

CREATE TABLE stocks (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    score NUMERIC(10,2),
    updated_time TIMESTAMP NOT NULL
);

CREATE TABLE stock_ranking (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL,
    ranking INTEGER NOT NULL,
    score NUMERIC(10,2) NOT NULL,
    calculate_time TIMESTAMP NOT NULL
);

CREATE INDEX idx_stock_symbol ON stocks(symbol);

INSERT INTO stocks (symbol, company_name, price, score, updated_time)
SELECT
    symbols.symbol,
    symbols.company_name,
    ROUND((100 + random() * 900)::numeric, 2),
    ROUND((50 + random() * 50)::numeric, 2),
    TIMESTAMP '2026-05-16 09:00:00' + (gs || ' minutes')::interval
FROM (
    VALUES
    ('AAPL', 'Apple Inc'),
    ('MSFT', 'Microsoft Corp'),
    ('GOOG', 'Alphabet Inc'),
    ('AMZN', 'Amazon Inc'),
    ('META', 'Meta Platforms'),
    ('TSLA', 'Tesla Inc'),
    ('NVDA', 'NVIDIA Corp'),
    ('NFLX', 'Netflix Inc'),
    ('ORCL', 'Oracle Corp'),
    ('IBM', 'IBM Corp'),
    ('INTC', 'Intel Corp')
) AS symbols(symbol, company_name)
CROSS JOIN generate_series(1,10) gs;
