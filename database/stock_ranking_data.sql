-- Insert 10 ranking test records
-- Based on generated stock data from init.sql

INSERT INTO stock_ranking
(symbol, ranking, score, calculate_time)
VALUES
('NVDA', 1, 145.23, '2026-05-16 12:00:00'),
('TSLA', 2, 132.88, '2026-05-16 12:00:00'),
('META', 3, 128.44, '2026-05-16 12:00:00'),
('NFLX', 4, 119.91, '2026-05-16 12:00:00'),
('AMZN', 5, 110.52, '2026-05-16 12:00:00'),
('MSFT', 6, 104.18, '2026-05-16 12:00:00'),
('AAPL', 7, 98.75, '2026-05-16 12:00:00'),
('GOOG', 8, 92.36, '2026-05-16 12:00:00'),
('ORCL', 9, 85.14, '2026-05-16 12:00:00'),
('IBM', 10, 79.63, '2026-05-16 12:00:00');