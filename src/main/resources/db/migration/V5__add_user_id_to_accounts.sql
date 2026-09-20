ALTER TABLE accounts
    ADD COLUMN user_id BIGINT REFERENCES users(id);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);