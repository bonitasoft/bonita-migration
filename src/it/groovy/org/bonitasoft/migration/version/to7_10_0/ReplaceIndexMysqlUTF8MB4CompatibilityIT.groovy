package org.bonitasoft.migration.version.to7_10_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.database.schema.ColumnDefinition
import spock.lang.Shared
import spock.lang.Specification

import java.util.stream.Collectors

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.MYSQL


class ReplaceIndexMysqlUTF8MB4CompatibilityIT extends Specification {

    @Shared
    def logger = new Logger()

    @Shared
    def migrationContext = new MigrationContext(logger: logger)

    @Shared
    def dbUnitHelper = new DBUnitHelper(migrationContext)
    ReplaceIndexMysqlUTF8MB4Compatibility migrationStep = new ReplaceIndexMysqlUTF8MB4Compatibility();

    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.10.0")
        dbUnitHelper.createTables("7_10_0")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["message_instance"] as String[])
    }

    def "should have migrated the index and removed correlation3 on MySQL but not on others RDBMSs"() {

        given:
        def correlation3ColumnBeforeMigration = dbUnitHelper.getIndexDefinition("message_instance", "idx_message_instance").columnDefinitions.stream().filter { it -> it.columnName.equalsIgnoreCase("correlation3") }.collect(Collectors.toList())
        assert correlation3ColumnBeforeMigration.size() == 1

        when:
        migrationStep.execute(migrationContext)

        then:
        def correlation3Column = dbUnitHelper.getIndexDefinition("message_instance", "idx_message_instance").columnDefinitions.stream().filter { it -> it.columnName.equalsIgnoreCase("correlation3") }.collect(Collectors.toList())
        if (migrationContext.dbVendor == MYSQL) {
            correlation3Column.isEmpty()
        } else {
            correlation3Column.size() == 1
        }

    }





}
