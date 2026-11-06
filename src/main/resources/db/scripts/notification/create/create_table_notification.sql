CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_notification_type CHECK (type IN ('BUDGET_ALERT', 'SECURITY_ALERT')),
    CONSTRAINT chk_notification_read CHECK (
        (is_read = TRUE AND read_at IS NOT NULL) OR (is_read = FALSE AND read_at IS NULL)
    )
);

CREATE INDEX idx_notification_user_created ON notification(user_id, created_at DESC, id DESC);
CREATE INDEX idx_notification_user_unread ON notification(user_id) WHERE is_read = FALSE;
