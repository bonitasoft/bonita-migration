package org.bonitasoft.update.version.to7_11_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.Logger
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Danila Mazour
 */
class RefactorPlatformColumnsIT extends Specification {

    @Shared
    def logger = new Logger()

    @Shared
    def updateContext = new UpdateContext(logger: logger)

    @Shared
    def dbUnitHelper = new DBUnitHelper(updateContext)
    RefactorPlatformColumns updateStep = new RefactorPlatformColumns()

    def setup() {
        dropTestTables()
        updateContext.setVersion("7.11.0")
        dbUnitHelper.createTables("7_11_0/platform")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["platform"] as String[])
    }

    def "should update all 3 columns from 'platform' table"() {
        when:
        updateStep.execute(updateContext)

        then:

        dbUnitHelper.hasColumnOnTable("platform", "version")
        dbUnitHelper.hasColumnOnTable("platform", "initial_bonita_version")
        dbUnitHelper.hasColumnOnTable("platform", "created_by")

        !dbUnitHelper.hasColumnOnTable("platform", "initialVersion")
        !dbUnitHelper.hasColumnOnTable("platform", "previousVersion")
    }

}

