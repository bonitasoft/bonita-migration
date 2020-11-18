package org.bonitasoft.migration.version.to7_12_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.version.to7_11_0.AddIndexOnArchFlownodeInstance
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Danila Mazour
 */
class AddIndexLG4LG2OnArchFlownodeInstanceIT extends Specification {

    @Shared
    def logger = new Logger()

    @Shared
    def migrationContext = new MigrationContext(logger: logger)

    @Shared
    def dbUnitHelper = new DBUnitHelper(migrationContext)
    AddIndexLG4LG2OnArchFlownodeInstance migrationStep = new AddIndexLG4LG2OnArchFlownodeInstance()


    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.12.0")
        dbUnitHelper.createTables("7_12_0/arch_flownode_instance")
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
        dbUnitHelper.hasIndexOnTable("arch_flownode_instance", "idx_lg4_lg2")
    }

}

