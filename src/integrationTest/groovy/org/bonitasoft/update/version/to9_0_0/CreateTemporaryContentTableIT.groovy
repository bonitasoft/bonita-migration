package org.bonitasoft.update.version.to9_0_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class CreateTemporaryContentTableIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private CreateTemporaryContentTable updateStep = new CreateTemporaryContentTable()

    def setup() {
        dropTestTables()
        updateContext.setVersion("9.0.0")
        dbUnitHelper.createTables("9_0_0")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["temporary_content", "sequence", "platform", "ref_biz_data_inst", "process_instance", "tenant"] as String[])
    }


    def "should create temporary_content_table"() {
        given:

        when:
        updateStep.execute(updateContext)

        then:
        updateContext.databaseHelper.hasTable("temporary_content")
        updateContext.databaseHelper.hasColumnOnTable("temporary_content", "id")
        updateContext.databaseHelper.hasColumnOnTable("temporary_content", "creationDate")
        updateContext.databaseHelper.hasColumnOnTable("temporary_content", "key_")
        updateContext.databaseHelper.hasColumnOnTable("temporary_content", "fileName")
        updateContext.databaseHelper.hasColumnOnTable("temporary_content", "mimeType")
        updateContext.databaseHelper.hasColumnOnTable("temporary_content", "content")

        // validate new sequence presence
        updateContext.databaseHelper.getSequenceValue(-1, 5) != null
    }
}
