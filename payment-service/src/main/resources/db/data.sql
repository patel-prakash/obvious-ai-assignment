-- Payment Service Sample Data

-- Insert sample payment data
INSERT INTO payment (transaction_id, order_id, product_code, quantity, amount, payment_mode, status, timestamp, failure_reason, stock_lock_reference_id)
VALUES
('TRX-2023-0001', 'ORD-2023-0001', 'PROD-001', 2, 199.99, 'CREDIT_CARD', 'SUCCESS', NOW() - INTERVAL 7 DAY, NULL, 'LOCK-0001'),
('TRX-2023-0002', 'ORD-2023-0002', 'PROD-002', 1, 599.99, 'PAYPAL', 'SUCCESS', NOW() - INTERVAL 6 DAY, NULL, 'LOCK-0002'),
('TRX-2023-0003', 'ORD-2023-0003', 'PROD-003', 3, 29.99, 'DEBIT_CARD', 'FAILED', NOW() - INTERVAL 5 DAY, 'Payment processing error', NULL),
('TRX-2023-0004', 'ORD-2023-0004', 'PROD-004', 1, 999.99, 'CREDIT_CARD', 'SUCCESS', NOW() - INTERVAL 4 DAY, NULL, 'LOCK-0003'),
('TRX-2023-0005', 'ORD-2023-0005', 'PROD-005', 2, 49.99, 'BANK_TRANSFER', 'REFUNDED', NOW() - INTERVAL 3 DAY, 'Customer requested refund', 'LOCK-0004'),
('TRX-2023-0006', 'ORD-2023-0006', 'PROD-001', 1, 99.99, 'CREDIT_CARD', 'PENDING', NOW() - INTERVAL 2 DAY, 'Inventory service unavailable', NULL),
('TRX-2023-0007', 'ORD-2023-0007', 'PROD-002', 1, 599.99, 'CREDIT_CARD', 'SUCCESS', NOW() - INTERVAL 1 DAY, NULL, 'LOCK-0005'),
('TRX-2023-0008', 'ORD-2023-0008', 'PROD-006', 2, 79.99, 'PAYPAL', 'SUCCESS', NOW(), NULL, 'LOCK-0006'); 