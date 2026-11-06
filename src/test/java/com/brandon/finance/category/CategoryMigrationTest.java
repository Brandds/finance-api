package com.brandon.finance.category;

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

class CategoryMigrationTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void masterChangelogAddsIconAndBackfillsExistingCategoriesBeforeMakingItRequired() throws Exception {
        try (var resources = new ClassLoaderResourceAccessor()) {
            var database = new PostgresDatabase();
            database.setConnection(new OfflineConnection(
                "offline:postgresql?changeLogFile=" + temporaryDirectory.resolve("changelog.csv"), resources));

            try (var liquibase = new Liquibase("db/changelog/db.changelog-master.xml", resources, database)) {
                var output = new StringWriter();
                liquibase.update(new Contexts(), new LabelExpression(), output);

                String sql = output.toString().replaceAll("\\s+", " ");
                String addColumn = "ALTER TABLE category ADD COLUMN icon VARCHAR(50);";
                String backfill = "UPDATE category SET icon = 'FOOD' WHERE icon IS NULL;";
                String requireIcon = "ALTER TABLE category ALTER COLUMN icon SET NOT NULL;";

                assertThat(sql).contains(addColumn, backfill, requireIcon);
                assertThat(sql.indexOf(addColumn)).isLessThan(sql.indexOf(backfill));
                assertThat(sql.indexOf(backfill)).isLessThan(sql.indexOf(requireIcon));
            }
        }
    }
}
