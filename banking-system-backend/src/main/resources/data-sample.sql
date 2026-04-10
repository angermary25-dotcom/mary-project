-- ================================================
-- Sample Data for Banking System
-- ================================================
-- NOTE: Passwords below are BCrypt-hashed versions of the plain text shown in comments.
-- The DataLoader (CommandLineRunner) automatically inserts this data on first run.
-- Use these SQL statements if you prefer manual insertion.

USE banking_system;

-- ================================================
-- USERS (password in comments)
-- ================================================
-- Password: 123456
INSERT INTO users (name, email, password, role) VALUES
('Mary Anger', 'mary@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'USER'),
('John Doe', 'john@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'USER');

-- Password: admin123
INSERT INTO users (name, email, password, role) VALUES
('Admin User', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'ADMIN');

-- ================================================
-- ACCOUNTS
-- ================================================
INSERT INTO accounts (user_id, balance) VALUES
(1, 5000.00),   -- Mary's account
(2, 3000.00),   -- John's account
(3, 10000.00);  -- Admin's account

-- ================================================
-- SAMPLE TRANSACTION
-- ================================================
INSERT INTO transactions (sender_account_id, receiver_account_id, amount, timestamp, status) VALUES
(1, 2, 500.00, NOW(), 'SUCCESS');

-- NOTE: If you use these SQL inserts, the BCrypt hashes above are placeholders.
-- For real usage, let the DataLoader (CommandLineRunner) handle insertion so
-- passwords are properly encrypted. Or generate real hashes using:
--   https://bcrypt-generator.com/
