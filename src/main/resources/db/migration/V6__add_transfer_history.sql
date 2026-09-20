CREATE TABLE transfers (
 id              BIGSERIAL PRIMARY KEY,
 from_account_id BIGINT NOT NULL REFERENCES accounts(id),
 to_account_id   BIGINT NOT NULL REFERENCES accounts(id),
 amount          NUMERIC(12, 2) NOT NULL,
 fee             NUMERIC(12, 2) NOT NULL DEFAULT 0,
 status          VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
 created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transfers_from ON transfers(from_account_id);
CREATE INDEX idx_transfers_to ON transfers(to_account_id);