package org.bonitasoft.update.version.to7_11_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.Logger
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Danila Mazour
 */
class AddIndexOnArchFlownodeInstanceIT extends Specification {

    @Shared
    def logger = new Logger()

    @Shared
    def updateContext = new UpdateContext(logger: logger)

    @Shared
    def dbUnitHelper = new DBUnitHelper(updateContext)
    AddIndexOnArchFlownodeInstance updateStep = new AddIndexOnArchFlownodeInstance()


    def setup() {
        dropTestTables()
        updateContext.setVersion("7.11.0")
        dbUnitHelper.createTables("7_11_0/arch_flownode_instance")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["arch_flownode_instance"] as String[])
    }

    def "should have added the index during the update"() {
        given:

        when:
        updateStep.execute(updateContext)

        then:
        dbUnitHelper.hasIndexOnTable("arch_flownode_instance", "idx_afi_kind_lg4")
    }

}

