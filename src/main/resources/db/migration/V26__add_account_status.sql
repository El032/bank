ALTER TABLE accounts
    ADD COLUMN status VARCHAR(20);

UPDATE accounts
SET status = CASE
                 WHEN is_active = true THEN 'ACTIVE'
                 ELSE 'CLOSED'
    END;

ALTER TABLE accounts
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE accounts
DROP COLUMN is_active;