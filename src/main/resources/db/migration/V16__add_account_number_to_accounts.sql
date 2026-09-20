ALTER TABLE accounts
    ADD COLUMN account_number VARCHAR(19);

UPDATE accounts
SET account_number =
        LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0') || '-' ||
        LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0') || '-' ||
        LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0') || '-' ||
        LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0')
WHERE account_number IS NULL;