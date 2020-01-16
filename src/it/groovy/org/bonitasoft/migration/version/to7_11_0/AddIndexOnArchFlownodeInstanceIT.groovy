package org.bonitasoft.migration.version.to7_11_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Danila Mazour
 */
class AddIndexOnArchFlownodeInstanceIT extends Specification {

    @Shared
    def logger = new Logger()

    @Shared
    def migrationContext = new MigrationContext(logger: logger)

    @Shared
    def dbUnitHelper = new DBUnitHelper(migrationContext)
    AddIndexOnArchFlownodeInstance migrationStep = new AddIndexOnArchFlownodeInstance()


    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.11.0")
        dbUnitHelper.createTables("7_11_0/arch_flownode_instance")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["arch_flownode_instance"] as String[])
    }

    def "should have added the index during the migration"() {

        given:

        when:
        migrationStep.execute(migrationContext)

        then:
        dbUnitHelper.hasIndexOnTable("arch_flownode_instance", "idx_afi_kind_lg4")

    }

}

