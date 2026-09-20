CREATE TABLE accounts (
  id          BIGSERIAL PRIMARY KEY,
  owner       VARCHAR(100) NOT NULL,
  balance     NUMERIC(12, 2) NOT NULL DEFAULT 0,
  is_active   BOOLEAN NOT NULL DEFAULT true,
  email       VARCHAR(100)
);