-- Initialize TechBank databases
CREATE DATABASE techbank_accounts;
CREATE DATABASE techbank_transactions;

-- Switch to accounts database and create tables
\c techbank_accounts;
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL UNIQUE,
    account_holder VARCHAR(255) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Switch to transactions database and create tables
\c techbank_transactions;
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    source_account VARCHAR(50) NOT NULL,
    destination_account VARCHAR(50) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_transactions_source ON transactions(source_account);
CREATE INDEX idx_transactions_destination ON transactions(destination_account);
