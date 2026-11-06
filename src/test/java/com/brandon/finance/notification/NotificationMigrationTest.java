package com.brandon.finance.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.OfflineConnection;
import liquibase.database.core.PostgresDatabase;
import liquibase.resource.ClassLoaderResourceAccessor;

class NotificationMigrationTest {
    @TempDir Path temporaryDirectory;

    @Test
    void masterIncludesNotificationTableWithOwnershipReadStateAndIndexes() throws Exception {
        try (var resources = new ClassLoaderResourceAccessor()) {
            var database = new PostgresDatabase();
            database.setConnection(new OfflineConnection(
                "offline:postgresql?changeLogFile=" + temporaryDirectory.resolve("changelog.csv"), resources));
            try (var liquibase = new Liquibase("db/changelog/db.changelog-master.xml", resources, database)) {
                var output = new StringWriter();
                liquibase.update(new Contexts(), new LabelExpression(), output);

                assertThat(output.toString().replaceAll("\\s+", " ")).contains(
                    "CREATE TABLE notification (", "is_read BOOLEAN NOT NULL DEFAULT FALSE",
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE",
                    "CHECK (type IN ('BUDGET_ALERT', 'SECURITY_ALERT'))",
                    "CREATE INDEX idx_notification_user_created", "CREATE INDEX idx_notification_user_unread");
            }
        }
    }
}
