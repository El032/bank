ALTER TABLE bank_cards
    ADD COLUMN status VARCHAR(20);

UPDATE bank_cards
SET status = 'ACTIVE';

ALTER TABLE bank_cards
    ALTER COLUMN status SET NOT NULL;