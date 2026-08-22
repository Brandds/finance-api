CREATE TABLE IF NOT EXISTS expense_tag (
    expense_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,

    PRIMARY KEY (expense_id, tag_id),

    CONSTRAINT fk_expense_tag_expense
        FOREIGN KEY (expense_id) REFERENCES expense(id),

    CONSTRAINT fk_expense_tag_tag
        FOREIGN KEY (tag_id) REFERENCES tag(id)
);
