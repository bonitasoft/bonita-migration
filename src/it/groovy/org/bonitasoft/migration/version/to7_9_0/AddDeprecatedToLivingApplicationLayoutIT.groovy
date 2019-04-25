/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.migration.version.to7_9_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

import static org.bonitasoft.migration.version.to7_9_0.AddDeprecatedToLivingApplicationLayout.DEFAULT_LAYOUT_NAME
import static org.bonitasoft.migration.version.to7_9_0.AddDeprecatedToLivingApplicationLayout.DEFAULT_LAYOUT_OLD_DISPLAY_NAME
import static org.bonitasoft.migration.version.to7_9_0.AddDeprecatedToLivingApplicationLayout.DEFAULT_LAYOUT_NEW_DISPLAY_NAME
import static org.bonitasoft.migration.version.to7_9_0.AddDeprecatedToLivingApplicationLayout.DEFAULT_LAYOUT_OLD_DESCRIPTION
import static org.bonitasoft.migration.version.to7_9_0.AddDeprecatedToLivingApplicationLayout.DEFAULT_LAYOUT_NEW_DESCRIPTION
import static org.bonitasoft.migration.version.to7_9_0.AddDeprecatedToLivingApplicationLayout.DEFAULT_LAYOUT_NAME

/**
 * @author Dumitru Corini
 */
class AddDeprecatedToLivingApplicationLayoutIT extends Specification {

    @Shared
    def logger = new Logger()

    @Shared
    def migrationContext = new MigrationContext(logger: logger)

    @Shared
    def dbUnitHelper = new DBUnitHelper(migrationContext)

    AddDeprecatedToLivingApplicationLayout migrationStep = new AddDeprecatedToLivingApplicationLayout()
    private static final DEFAULT_LAYOUT_OLD_DESCRIPTION = 'This is the default layout V5 definition for a newly-created application. It was created using the UI designer, so you can export it and edit it with the UI designer. It contains a horizontal menu widget and an iframe widget. The menu structure  is defined in the application navigation. The application pages are displayed in the iframe.'
    private static final DEFAULT_LAYOUT_OLD_DISPLAY_NAME = 'Default living application layout'

    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.9.0")
        dbUnitHelper.createTables("7_9_0/page", "page")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["page"] as String[])
    }

    def "should update page when the page name is custompage_defaultlayout"() {

        given:
        byte[] content = "myValue".getBytes()
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 1, '${DEFAULT_LAYOUT_NAME}', '${DEFAULT_LAYOUT_OLD_DISPLAY_NAME}', '${DEFAULT_LAYOUT_OLD_DESCRIPTION}', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'contentName', $content, 'a content', 1)
""")
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 2, 'custompage_defaultlayoutCustom', 'Default living application layout Custom', 'This is the default layout V5 definition for a newly-created application. It was created using the UI designer, so you can export it and edit it with the UI designer. It contains a horizontal menu widget and an iframe widget. The menu structure  is defined in the application navigation. The application pages are displayed in the iframe. Custom', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'contentName', $content, 'a content', 1)
""")
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 3, 'custompage_htmlexample', 'HTML example page', 'HTML and Javascript example of custom page source structure (in English).', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'a html page', $content, 'a content', 4)
""")
        when:
        migrationStep.execute(migrationContext)

        then:
        List updatedRows = migrationContext.sql.rows("SELECT id, displayName, description FROM page WHERE displayName = '${DEFAULT_LAYOUT_NEW_DISPLAY_NAME}'")
        updatedRows.size() == 1
        updatedRows[0].id == 1
        updatedRows[0].displayName == DEFAULT_LAYOUT_NEW_DISPLAY_NAME
        updatedRows[0].description == DEFAULT_LAYOUT_NEW_DESCRIPTION
    }

    def "should update all pages in two tenants"() {

        given:
        byte[] content = "myValue".getBytes()
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 1, '${DEFAULT_LAYOUT_NAME}', '${DEFAULT_LAYOUT_OLD_DISPLAY_NAME}', '${DEFAULT_LAYOUT_OLD_DESCRIPTION}', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'contentName', $content, 'a content', 1)
""")
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (2, 2, '${DEFAULT_LAYOUT_NAME}', '${DEFAULT_LAYOUT_OLD_DISPLAY_NAME}', '${DEFAULT_LAYOUT_OLD_DESCRIPTION}', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'contentName', $content, 'a content', 1)
""")
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 3, 'custompage_htmlexample', 'HTML example page', 'HTML and Javascript example of custom page source structure (in English).', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'a html page', $content, 'a content', 4)
""")
        when:
        migrationStep.execute(migrationContext)

        then:
        List updatedRows = migrationContext.sql.rows("SELECT tenantId FROM page WHERE displayName ='${DEFAULT_LAYOUT_NEW_DISPLAY_NAME}'")
        updatedRows.size() == 2
        updatedRows[0].tenantId == 1
        updatedRows[1].tenantId == 2
    }

}
