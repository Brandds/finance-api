CREATE TABLE expense (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    date DATE NOT NULL,

    user_id BIGINT NOT NULL,
    category_id BIGINT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_expense_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_expense_category
        FOREIGN KEY (category_id)
        REFERENCES category(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_expense_amount_positive
        CHECK (amount > 0)
);