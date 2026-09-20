ALTER TABLE accounts
ALTER COLUMN account_number TYPE VARCHAR(24);

UPDATE accounts
SET account_number =
        '4081-7910-' ||
        LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0') || '-' ||
        LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0') || '-' ||
        LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0')
WHERE account_number IS NOT NULL;

ALTER TABLE accounts
    ADD CONSTRAINT uk_accounts_account_number
        UNIQUE (account_number);