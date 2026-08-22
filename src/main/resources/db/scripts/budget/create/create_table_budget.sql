CREATE TABLE IF NOT EXISTS budget (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    limit_amount NUMERIC(10,2) NOT NULL,
    month INT NOT NULL CHECK (month BETWEEN 1 AND 12),
    year INT NOT NULL,

    CONSTRAINT fk_budget_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_budget_category
        FOREIGN KEY (category_id) REFERENCES category(id),

    CONSTRAINT uq_budget UNIQUE (user_id, category_id, month, year)
);
