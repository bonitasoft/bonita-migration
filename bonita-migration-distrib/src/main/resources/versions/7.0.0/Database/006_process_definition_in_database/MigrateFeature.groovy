import org.bonitasoft.migration.core.MigrationUtil
import org.bonitasoft.migration.versions.v7_0_0.ProcessDefinitionInDatabaseMigration

MigrationUtil.executeDefaultSqlFile(feature, dbVendor, sql);

new ProcessDefinitionInDatabaseMigration(sql, dbVendor, bonitaHome).migrate()

MigrationUtil.executeSqlFile(feature, dbVendor, "AddNotNull", [:], sql, false);
