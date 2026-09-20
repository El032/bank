CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50) NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    full_name  VARCHAR(200),
    active     BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);