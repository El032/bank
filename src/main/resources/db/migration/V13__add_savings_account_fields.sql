ALTER TABLE accounts
    ADD COLUMN account_type VARCHAR(31) NOT NULL DEFAULT 'BANK';

ALTER TABLE accounts
    ADD COLUMN interest_rate NUMERIC(5, 2);

ALTER TABLE accounts
    ADD COLUMN opened_at DATE;

ALTER TABLE accounts
    ADD COLUMN last_interest DATE;

ALTER TABLE accounts
    ALTER COLUMN account_type DROP DEFAULT;