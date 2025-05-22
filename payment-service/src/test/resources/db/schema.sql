-- Payment Service Test Database Schema for H2

-- Drop tables if they exist
DROP TABLE IF EXISTS payments;

-- Create payment table with H2 compatible syntax
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    order_id VARCHAR(100) NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_mode VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    failure_reason VARCHAR(255),
    stock_lock_reference_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes separately for H2 compatibility
CREATE INDEX IF NOT EXISTS idx_transaction_id ON payments(transaction_id);
CREATE INDEX IF NOT EXISTS idx_order_id ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_product_code ON payments(product_code);
CREATE INDEX IF NOT EXISTS idx_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_stock_lock_reference_id ON payments(stock_lock_reference_id); 