CREATE TABLE IF NOT EXISTS top_rec_absolute_increase (
    rank INT NOT NULL,
    updated_date DATE NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    price_high NUMERIC(12, 4) NOT NULL,
    price_low NUMERIC(12, 4) NOT NULL,
    volume BIGINT NOT NULL,
    price_increase_amt NUMERIC(12, 4) NOT NULL, -- The absolute dollar increase (e.g., +$5.50)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Primary Key ensures a clean ranking structure per day
    PRIMARY KEY (rank, updated_date)
);

-- Index to quickly pull the latest rankings for your user interface
CREATE INDEX IF NOT EXISTS idx_absolute_rec_date ON top_rec_absolute_increase (updated_date DESC);

CREATE TABLE IF NOT EXISTS top_rec_percentage_increase (
    rank INT NOT NULL,
    updated_date DATE NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    price_high NUMERIC(12, 4) NOT NULL,
    price_low NUMERIC(12, 4) NOT NULL,
    volume BIGINT NOT NULL,
    price_increase_pct NUMERIC(12, 4) NOT NULL, -- The percentage increase (e.g., 12.5500 for 12.55%)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Primary Key ensures a clean ranking structure per day
    PRIMARY KEY (rank, updated_date)
);

-- Index to quickly pull the latest rankings for your user interface
CREATE INDEX IF NOT EXISTS idx_percentage_rec_date ON top_rec_percentage_increase (updated_date DESC);