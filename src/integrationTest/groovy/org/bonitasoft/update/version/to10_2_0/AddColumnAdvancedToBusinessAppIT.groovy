package org.bonitasoft.update.version.to10_2_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class AddColumnAdvancedToBusinessAppIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private AddColumnAdvancedToBusinessApp updateStep = new AddColumnAdvancedToBusinessApp()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("10_2_0")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["business_app"] as String[])
    }

    def "should add new column to business_app table"() {
        given:
        assert !updateContext.databaseHelper.hasColumnOnTable("business_app", "advanced")

        when:
        updateStep.execute(updateContext)

        then:
        updateContext.databaseHelper.hasTable("business_app")
        updateContext.databaseHelper.hasColumnOnTable("business_app", "advanced")
    }

}
