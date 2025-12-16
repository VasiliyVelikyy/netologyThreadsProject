DROP TABLE IF EXISTS bank_accounts;

CREATE TABLE bank_accounts (
                               account_number VARCHAR(20) PRIMARY KEY,
                               balance DOUBLE NOT NULL
);

-- Таблица: account_info
CREATE TABLE account_info (
                              account_number VARCHAR(20) NOT NULL PRIMARY KEY,
                              currency VARCHAR(3) NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              created_at TIMESTAMP NOT NULL,
                              last_transaction_at TIMESTAMP,
                              FOREIGN KEY (account_number) REFERENCES bank_accounts(account_number) ON DELETE CASCADE
);

-- Таблица: account_risk
CREATE TABLE account_risk (
                              account_number VARCHAR(20) NOT NULL PRIMARY KEY,
                              risk_score INT CHECK (risk_score >= 0 AND risk_score <= 100),
                              last_credit TIMESTAMP,
                              FOREIGN KEY (account_number) REFERENCES bank_accounts(account_number) ON DELETE CASCADE
);

