-- Payment Service Database Schema

-- Drop tables if they exist
DROP TABLE IF EXISTS payment;

-- Create payment table
CREATE TABLE IF NOT EXISTS payment (
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_order_id (order_id),
    INDEX idx_product_code (product_code),
    INDEX idx_status (status),
    INDEX idx_stock_lock_reference_id (stock_lock_reference_id)
); 