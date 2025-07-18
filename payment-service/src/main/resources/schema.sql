CREATE TABLE IF NOT EXISTS accounts(
    id  bigserial PRIMARY KEY,
    user_name    VARCHAR(255)   NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.0
);