DROP TABLE IF EXISTS bank_accounts;

CREATE TABLE bank_accounts (
                               account_number VARCHAR(20) PRIMARY KEY,
                               balance DOUBLE NOT NULL
);
