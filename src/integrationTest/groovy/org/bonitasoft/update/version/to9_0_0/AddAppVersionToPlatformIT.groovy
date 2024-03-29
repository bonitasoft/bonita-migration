package org.bonitasoft.update.version.to9_0_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class AddAppVersionToPlatformIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private AddAppVersionToPlatform updateStep = new AddAppVersionToPlatform()

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

    def "should create new column on platform table"() {
        given:

        when:
        updateStep.execute(updateContext)

        then:
        updateContext.databaseHelper.hasTable("platform")
        updateContext.databaseHelper.hasColumnOnTable("platform", "application_version")
    }
}
