CREATE TABLE stock_symbols (
    symbol VARCHAR(20) PRIMARY KEY,
    currency VARCHAR(10),
    description TEXT,
    display_symbol VARCHAR(20),
    figi VARCHAR(20),
    figi_composite VARCHAR(20),
    mic VARCHAR(10),
    share_class_figi VARCHAR(20),
    type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);