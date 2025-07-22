-- Create transactions table
CREATE TABLE transactions (
    id VARCHAR(36) PRIMARY KEY,
    sender_id VARCHAR(36),
    receiver_id VARCHAR(36) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_transactions_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    CONSTRAINT fk_transactions_receiver FOREIGN KEY (receiver_id) REFERENCES users(id),
    
    -- Check constraint to ensure amount is positive
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
);

-- Add indexes for efficient querying
CREATE INDEX idx_transactions_sender ON transactions(sender_id);
CREATE INDEX idx_transactions_receiver ON transactions(receiver_id);
CREATE INDEX idx_transactions_timestamp ON transactions(timestamp);

-- Add composite index for user transaction history queries
CREATE INDEX idx_transactions_user_time ON transactions(sender_id, receiver_id, timestamp DESC);