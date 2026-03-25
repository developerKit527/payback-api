-- Additional Merchant Data for Payback API
-- This script inserts 6 new merchant partners with their categories and cashback percentages

-- Insert Zomato (food delivery, 5%)
INSERT INTO merchants (name, logo_url, cashback_rate, manual_tracking_url, click_count)
VALUES ('Zomato', 'https://example.com/zomato-logo.png', 5.0, 'https://tracking.example.com/zomato', 0)
ON CONFLICT (name) DO NOTHING;

-- Insert Swiggy (food delivery, 4%)
INSERT INTO merchants (name, logo_url, cashback_rate, manual_tracking_url, click_count)
VALUES ('Swiggy', 'https://example.com/swiggy-logo.png', 4.0, 'https://tracking.example.com/swiggy', 0)
ON CONFLICT (name) DO NOTHING;

-- Insert MakeMyTrip (travel, 6%)
INSERT INTO merchants (name, logo_url, cashback_rate, manual_tracking_url, click_count)
VALUES ('MakeMyTrip', 'https://example.com/makemytrip-logo.png', 6.0, 'https://tracking.example.com/makemytrip', 0)
ON CONFLICT (name) DO NOTHING;

-- Insert boAt (electronics, 8%)
INSERT INTO merchants (name, logo_url, cashback_rate, manual_tracking_url, click_count)
VALUES ('boAt', 'https://example.com/boat-logo.png', 8.0, 'https://tracking.example.com/boat', 0)
ON CONFLICT (name) DO NOTHING;

-- Insert Meesho (fashion, 10%)
INSERT INTO merchants (name, logo_url, cashback_rate, manual_tracking_url, click_count)
VALUES ('Meesho', 'https://example.com/meesho-logo.png', 10.0, 'https://tracking.example.com/meesho', 0)
ON CONFLICT (name) DO NOTHING;

-- Insert Tata CLiQ (electronics/fashion, 7%)
INSERT INTO merchants (name, logo_url, cashback_rate, manual_tracking_url, click_count)
VALUES ('Tata CLiQ', 'https://example.com/tatacliq-logo.png', 7.0, 'https://tracking.example.com/tatacliq', 0)
ON CONFLICT (name) DO NOTHING;
