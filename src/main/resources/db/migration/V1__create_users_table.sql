-- Create users table
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    pin_hash VARCHAR(255) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add index for phone number lookups
CREATE INDEX idx_users_phone ON users(phone);

-- Add constraint to ensure balance is not negative
ALTER TABLE users ADD CONSTRAINT chk_balance_not_negative CHECK (balance >= 0.00);