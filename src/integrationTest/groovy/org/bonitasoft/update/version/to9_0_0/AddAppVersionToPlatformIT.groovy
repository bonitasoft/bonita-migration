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
        updateContext.setVersion("9.0.0")
        dbUnitHelper.createTables("9_0_0")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["platform"] as String[])
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
