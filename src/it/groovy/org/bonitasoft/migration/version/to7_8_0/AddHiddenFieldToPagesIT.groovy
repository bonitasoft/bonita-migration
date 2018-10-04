package org.bonitasoft.migration.version.to7_8_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Danila Mazour
 */
class AddHiddenFieldToPagesIT extends Specification {

    @Shared
    Logger logger = new Logger()

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    AddHiddenFieldToPages migrationStep = new AddHiddenFieldToPages()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_8_0")

    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["page"] as String[])
    }

    def "should add a hidden field defaulting to false"() {

        given:

        byte[] content = "myValue".getBytes()
        migrationContext.sql.execute("""
insert into page(tenantId , id, name, displayName, description, installationDate, installedBy, provided, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
values (1, 1, 'page1','page 1', 'my first page', 1, 1, ${dbUnitHelper.falseValue()}, 1, 1, 'a page', $content, 'a content', 1)
""")
        migrationContext.sql.execute("""
insert into page(tenantId , id, name, displayName, description, installationDate, installedBy, provided, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
values (1, 1, 'page2','page 2' , 'my second page', 1, 1, ${dbUnitHelper.falseValue()}, 1, 1, 'a page', $content, 'a content', 1)
""")
        migrationContext.sql.execute("""
insert into page(tenantId , id, name, displayName, description, installationDate, installedBy, provided, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
values (1, 1, 'page3','page 2' , 'my third page', 1, 1, ${dbUnitHelper.falseValue()}, 1, 1, 'a page', $content, 'a content', 1)
""")

        when:
        migrationStep.execute(migrationContext)

        then: 'page has a new field hidden set to false'
        def hiddenpage = migrationContext.sql.rows("SELECT name,hidden FROM page WHERE hidden = ${dbUnitHelper.falseValue()}")
        hiddenpage.size() == 3

    }

}
