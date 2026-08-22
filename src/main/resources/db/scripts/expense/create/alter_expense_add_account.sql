ALTER TABLE expense
ADD COLUMN IF NOT EXISTS account_id BIGINT;

ALTER TABLE expense
ADD CONSTRAINT fk_expense_account
FOREIGN KEY (account_id) REFERENCES account(id);
