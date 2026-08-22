CREATE TABLE IF NOT EXISTS financial_transaction (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255),
    amount NUMERIC(10,2) NOT NULL CHECK (amount > 0),
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    type VARCHAR(10) NOT NULL,
    category_id BIGINT,
    account_id BIGINT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transaction_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_transaction_category
        FOREIGN KEY (category_id) REFERENCES category(id),

    CONSTRAINT fk_transaction_account
        FOREIGN KEY (account_id) REFERENCES account(id)
);
