ALTER TABLE accounts
    ADD COLUMN credit_limit DECIMAL(12,2),
    ADD COLUMN first_transaction_date DATE,
    ADD COLUMN next_payment_date DATE;