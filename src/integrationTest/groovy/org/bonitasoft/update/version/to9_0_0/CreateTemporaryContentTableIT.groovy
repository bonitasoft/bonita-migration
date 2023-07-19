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
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["temporary_content"] as String[])
    }


    def "should create temporary_content_table"() {
        given:

        when:
        updateStep.execute(updateContext)

        then:
        assert updateContext.databaseHelper.hasTable("temporary_content")
        assert updateContext.databaseHelper.hasColumnOnTable("temporary_content", "id")
        assert updateContext.databaseHelper.hasColumnOnTable("temporary_content", "creationDate")
        assert updateContext.databaseHelper.hasColumnOnTable("temporary_content", "key_")
        assert updateContext.databaseHelper.hasColumnOnTable("temporary_content", "fileName")
        assert updateContext.databaseHelper.hasColumnOnTable("temporary_content", "mimeType")
        assert updateContext.databaseHelper.hasColumnOnTable("temporary_content", "content")
    }
}
