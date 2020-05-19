package org.bonitasoft.migration.version.to7_11_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

class AddIndexOnProcessCommentsIT extends Specification {

    @Shared
    def logger = new Logger()

    @Shared
    def migrationContext = new MigrationContext(logger: logger)

    @Shared
    def dbUnitHelper = new DBUnitHelper(migrationContext)
    AddIndexOnProcessComments migrationStep = new AddIndexOnProcessComments()


    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.11.0")
        dbUnitHelper.createTables("7_11_0/process_comment")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["process_comment"] as String[])
    }

    def "should have added the index during the migration"() {
        given:

        when:
        migrationStep.execute(migrationContext)

        then:
        dbUnitHelper.hasIndexOnTable("process_comment", "idx1_process_comment")
    }

}

