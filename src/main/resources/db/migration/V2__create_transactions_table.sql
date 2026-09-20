CREATE TABLE transactions (
  id          BIGSERIAL PRIMARY KEY,
  account_id  BIGINT NOT NULL REFERENCES accounts(id),
  amount      NUMERIC(12, 2) NOT NULL,
  type        VARCHAR(20) NOT NULL,
  created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);