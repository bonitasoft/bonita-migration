package org.bonitasoft.update.version.to9_0_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class AddMaintenanceMessageToPlatformIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private AddMaintenanceMessageToPlatform updateStep = new AddMaintenanceMessageToPlatform()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("9_0_0")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["temporary_content", "sequence", "platform", "ref_biz_data_inst", "process_instance", "tenant"] as String[])
    }

    def "should create new columns maintenance_message & maintenance_message_active on platform table"() {
        given:

        when:
        updateStep.execute(updateContext)

        then:
        updateContext.databaseHelper.hasTable("platform")
        updateContext.databaseHelper.hasColumnOnTable("platform", "maintenance_message")
        updateContext.databaseHelper.hasColumnOnTable("platform", "maintenance_message_active")
    }
}
