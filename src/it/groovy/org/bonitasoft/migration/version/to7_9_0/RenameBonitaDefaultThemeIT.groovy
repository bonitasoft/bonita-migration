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

/**
 * @author Dumitru Corini
 */
class RenameBonitaDefaultThemeIT extends Specification {

    @Shared
    def logger = new Logger()

    @Shared
    def migrationContext = new MigrationContext(logger: logger)

    @Shared
    def dbUnitHelper = new DBUnitHelper(migrationContext)

    RenameBonitaDefaultTheme migrationStep = new RenameBonitaDefaultTheme()

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

    def "should update page where name is custompage_bonitadefaulttheme"() {

        given:
        byte[] content = "myValue".getBytes()
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 1, 'custompage_bonitadefaulttheme', 'Bonita default theme', 'Application theme based on Bonita theme.', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'bonita-default-theme.zip', $content, 'a content', 1)
""")
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 2, 'custompage_htmlexample', 'HTML example page', 'HTML and Javascript example of custom page source structure (in English).', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'a html page', $content, 'a content', 2)
""")
        when:
        migrationStep.execute(migrationContext)

        then:
        List updatedRows = migrationContext.sql.rows("SELECT id FROM page WHERE name ='custompage_themeBonita'")
        updatedRows.size() == 1
        updatedRows[0].id == 1
    }
    
    def "should update page name, displayName and contentName"() {
        given:
        byte[] content = "myValue".getBytes()
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 1, 'custompage_bonitadefaulttheme', 'Bonita default theme', 'Application theme based on Bonita theme.', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'bonita-default-theme.zip', $content, 'a content', 1)
""")
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 2, 'custompage_bonitadefaultthemeCustom', 'Bonita default theme', 'Application theme based on Bonita theme.', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'bonita-default-theme-custom.zip', $content, 'a content', 2)
""")
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 3, 'custompage_bonitadefaultthemeCustom', 'Bonita theme Custom', 'Application theme based on Bonita theme.', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'bonita-default-theme.zip', $content, 'a content', 3)
""")
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 4, 'custompage_htmlexample', 'HTML example page', 'HTML and Javascript example of custom page source structure (in English).', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'a html page', $content, 'a content', 4)
""")
        when:
        migrationStep.execute(migrationContext)

        then:
        List updatedNameRows = migrationContext.sql.rows("SELECT id FROM page WHERE name ='custompage_themeBonita'")
        updatedNameRows.size() == 1
        updatedNameRows[0].id == 1
        List updatedDisplayNameRows = migrationContext.sql.rows("SELECT id FROM page WHERE displayName ='Bonita theme'")
        updatedDisplayNameRows.size() == 1
        updatedDisplayNameRows[0].id == 1
        List updatedContentNameRows = migrationContext.sql.rows("SELECT id FROM page WHERE contentName ='bonita-theme.zip'")
        updatedContentNameRows.size() == 1
        updatedContentNameRows[0].id == 1
    }

    def "should update all pages where page name is custompage_bonitadefaulttheme in two tenants"() {
        given:
        byte[] content = "myValue".getBytes()
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (2, 1, 'custompage_bonitadefaulttheme', 'Bonita default theme', 'Application theme based on Bonita theme.', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'bonita-default-theme.zip', $content, 'a content', 1)
""")
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 2, 'custompage_bonitadefaulttheme', 'Bonita default theme', 'Application theme based on Bonita theme.', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'bonita-default-theme.zip', $content, 'a content', 2)
""")
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 3, 'custompage_themeBonitaCustom', 'Bonita theme', 'Application theme based on Bonita theme.', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'bonita-default-theme-custom.zip', $content, 'a content', 3)
""")
        when:
        migrationStep.execute(migrationContext)

        then:
        List updatedRows = migrationContext.sql.rows("SELECT tenantId FROM page WHERE name ='custompage_themeBonita' ORDER BY tenantId")
        updatedRows.size() == 2

        updatedRows[0].tenantId == 1
        updatedRows[1].tenantId == 2
    }

    def "should not migrate if a theme with the name already exists for the same tenant"() {
        given:
        byte[] content = "myValue".getBytes()
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 1, 'custompage_bonitadefaulttheme', 'Bonita default theme', 'Application theme based on Bonita theme.', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'bonita-default-theme.zip', $content, 'a content', 1)
""")
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 2, 'custompage_themeBonita', 'Bonita default theme custom', 'Application theme based on Bonita theme.', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'bonita-default-theme-custom.zip', $content, 'a content', 2)
""")
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 3, 'custompage_themeBonitaCustom', 'Bonita theme', 'Application theme based on Bonita theme.', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'bonita-default-theme-custom.zip', $content, 'a content', 3)
""")

        when:
        migrationStep.execute(migrationContext)

        then:
        List unupdatedRows = migrationContext.sql.rows("SELECT id FROM page WHERE name='custompage_bonitadefaulttheme'")
        unupdatedRows.size() == 1

        unupdatedRows[0].id == 1

        List unchangedRows = migrationContext.sql.rows("SELECT displayName, contentName FROM page WHERE name='custompage_themeBonita'")
        unchangedRows.size() == 1

        unchangedRows[0].displayName == "Bonita default theme custom"
        unchangedRows[0].contentName == "bonita-default-theme-custom.zip"
    }

    def "should migrate a tenant if he does not have a theme with the name, while another has any"() {
        given:
        byte[] content = "myValue".getBytes()
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 1, 'custompage_bonitadefaulttheme', 'Bonita default theme', 'Application theme based on Bonita theme.', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'bonita-default-theme.zip', $content, 'a content', 1)
""")
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 2, 'custompage_themeBonita', 'Bonita default theme custom', 'Application theme based on Bonita theme.', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'bonita-default-theme-custom.zip', $content, 'a content', 2)
""")
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 3, 'custompage_themeBonitaCustom', 'Bonita theme', 'Application theme based on Bonita theme.', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'bonita-default-theme-custom.zip', $content, 'a content', 3)
""")
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (2, 4, 'custompage_bonitadefaulttheme', 'Bonita default theme', 'Application theme based on Bonita theme.', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'bonita-default-theme.zip', $content, 'a content', 1)
""")
        migrationContext.sql.execute("""
INSERT INTO page (tenantId, id, name, displayName, description, installationDate, installedBy, provided, hidden, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (2, 5, 'custompage_themeBonitaCustom', 'Bonita theme', 'Application theme based on Bonita theme.', 1, 1, ${dbUnitHelper.trueValue()}, ${dbUnitHelper.falseValue()}, 1, 1, 'bonita-default-theme-custom.zip', $content, 'a content', 3)
""")
        when:
        migrationStep.execute(migrationContext)

        then:
        List unupdatedRows = migrationContext.sql.rows("SELECT id FROM page WHERE name='custompage_bonitadefaulttheme'")
        unupdatedRows.size() == 1

        unupdatedRows[0].id == 1

        List updatedRows = migrationContext.sql.rows("SELECT id FROM page WHERE name='custompage_themeBonita' AND tenantId='2'")
        updatedRows.size() == 1

        updatedRows[0].id == 4
    }
}
