-- Test data for payments

-- Clear existing data
DELETE FROM payments;

-- Insert sample payments for testing
INSERT INTO payments (transaction_id, order_id, product_code, quantity, amount, payment_mode, status, timestamp, failure_reason, stock_lock_reference_id)
VALUES
  ('txn-test-001', 'order-test-001', 'PROD-001', 2, 100.00, 'CREDIT_CARD', 'SUCCESS', CURRENT_TIMESTAMP, NULL, 'lock-test-001'),
  ('txn-test-002', 'order-test-002', 'PROD-002', 1, 50.00, 'DEBIT_CARD', 'SUCCESS', CURRENT_TIMESTAMP, NULL, 'lock-test-002'),
  ('txn-test-003', 'order-test-003', 'PROD-003', 5, 250.00, 'WALLET', 'FAILED', CURRENT_TIMESTAMP, 'Insufficient funds', NULL),
  ('txn-test-004', 'order-test-004', 'PROD-004', 3, 150.00, 'BANK_TRANSFER', 'PENDING', CURRENT_TIMESTAMP, 'Processing payment', 'lock-test-004'),
  ('txn-test-005', 'order-test-005', 'PROD-005', 1, 75.00, 'UPI', 'REFUNDED', CURRENT_TIMESTAMP, 'Customer requested refund', 'lock-test-005'); 