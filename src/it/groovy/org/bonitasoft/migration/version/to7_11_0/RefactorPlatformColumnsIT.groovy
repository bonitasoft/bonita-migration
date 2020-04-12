package org.bonitasoft.migration.version.to7_11_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Danila Mazour
 */
class RefactorPlatformColumnsIT extends Specification {

    @Shared
    def logger = new Logger()

    @Shared
    def migrationContext = new MigrationContext(logger: logger)

    @Shared
    def dbUnitHelper = new DBUnitHelper(migrationContext)
    RefactorPlatformColumns migrationStep = new RefactorPlatformColumns()

    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.11.0")
        dbUnitHelper.createTables("7_11_0/platform")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["platform"] as String[])
    }

    def "should migrate all 3 columns from 'platform' table"() {
        when:
        migrationStep.execute(migrationContext)

        then:

        dbUnitHelper.hasColumnOnTable("platform", "version")
        dbUnitHelper.hasColumnOnTable("platform", "initial_bonita_version")
        dbUnitHelper.hasColumnOnTable("platform", "created_by")

        !dbUnitHelper.hasColumnOnTable("platform", "initialVersion")
        !dbUnitHelper.hasColumnOnTable("platform", "previousVersion")
    }

}

