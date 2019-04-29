package org.bonitasoft.migration.version.to7_9_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Danila Mazour
 */
class AddIndexActivityKindOnFlownodeInstanceIT extends Specification {

    @Shared
    def logger = new Logger()

    @Shared
    def migrationContext = new MigrationContext(logger: logger)

    @Shared
    def dbUnitHelper = new DBUnitHelper(migrationContext)
    AddIndexActivityKindOnFlownodeInstance migrationStep = new AddIndexActivityKindOnFlownodeInstance()


    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.9.0")
        dbUnitHelper.createTables("7_9_0/flownode_instance")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["flownode_instance"] as String[])
    }

    def "should have added the index during the migration"(){

        given:

        when:
        migrationStep.execute(migrationContext)

        then:
        dbUnitHelper.hasIndexOnTable("flownode_instance", "idx_fni_activity_instance_id_kind")

    }

}
