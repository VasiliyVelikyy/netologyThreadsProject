INSERT INTO bank_accounts (account_number, balance)
VALUES ('ACC001', 100000.0);
INSERT INTO bank_accounts (account_number, balance)
VALUES ('ACC002', 100000.0);
INSERT INTO bank_accounts (account_number, balance)
VALUES ('ACC003', 100000.0);
INSERT INTO bank_accounts (account_number, balance)
VALUES ('ACC004', 100000.0);
INSERT INTO bank_accounts (account_number, balance)
VALUES ('ACC005', 100000.0);
INSERT INTO bank_accounts (account_number, balance)
VALUES ('ACC006', 100000.0);
INSERT INTO bank_accounts (account_number, balance)
VALUES ('ACC007', 100000.0);
INSERT INTO bank_accounts (account_number, balance)
VALUES ('ACC008', 100000.0);



INSERT INTO account_info (account_number, currency, status, created_at, last_transaction_at)
VALUES ('ACC001', 'USD', 'ACTIVE', '2023-01-10 09:00:00', '2025-12-15 14:30:00'),
       ('ACC002', 'EUR', 'ACTIVE', '2022-11-05 11:20:00', '2025-12-16 10:15:00'),
       ('ACC003', 'USD', 'BLOCKED', '2023-03-22 16:45:00', '2025-10-01 09:00:00'),
       ('ACC004', 'GBP', 'ACTIVE', '2024-02-14 13:10:00', '2025-12-10 17:20:00'),
       ('ACC005', 'CHF', 'ACTIVE', '2023-07-19 08:30:00', '2025-12-14 11:45:00'),
       ('ACC006', 'USD', 'CLOSED', '2022-05-30 14:00:00', '2024-08-20 16:00:00'),
       ('ACC007', 'USD', 'ACTIVE', '2023-12-01 10:00:00', '2025-12-16 09:00:00'),
       ('ACC008', 'EUR', 'ACTIVE', '2024-05-11 12:15:00', '2025-12-12 15:30:00');


INSERT INTO account_risk (account_number, risk_score, last_credit)
VALUES ('ACC001', 20, '2024-06-15 00:00:00'),
       ('ACC002', 85, '2023-11-20 00:00:00'),
       ('ACC003', 90, '2022-09-10 00:00:00'),
       ('ACC004', 10, NULL),
       ('ACC005', 35, '2025-01-22 00:00:00'),
       ('ACC006', 0, NULL),
       ('ACC007', 50, '2024-12-05 00:00:00'),
       ('ACC008', 25, '2025-03-18 00:00:00');
