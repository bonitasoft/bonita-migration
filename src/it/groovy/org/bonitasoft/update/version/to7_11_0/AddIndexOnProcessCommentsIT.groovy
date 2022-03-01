package org.bonitasoft.update.version.to7_11_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.Logger
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class AddIndexOnProcessCommentsIT extends Specification {

    @Shared
    def logger = new Logger()

    @Shared
    def updateContext = new UpdateContext(logger: logger)

    @Shared
    def dbUnitHelper = new DBUnitHelper(updateContext)
    AddIndexOnProcessComments updateStep = new AddIndexOnProcessComments()


    def setup() {
        dropTestTables()
        updateContext.setVersion("7.11.0")
        dbUnitHelper.createTables("7_11_0/process_comment")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["process_comment"] as String[])
    }

    def "should have added the index during the update"() {
        given:

        when:
        updateStep.execute(updateContext)

        then:
        dbUnitHelper.hasIndexOnTable("process_comment", "idx1_process_comment")
    }

}

