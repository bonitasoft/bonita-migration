/**
 * Copyright (C) 2017 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_3_3

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.database.ConfigurationHelper
import org.bonitasoft.migration.core.database.DatabaseHelper
import spock.lang.Specification

import static org.bonitasoft.migration.version.to7_3_3.FixProcessSupervisorPermissionRuleScript.resourceName

/**
 * @author Laurent Leseigneur
 */
class FixProcessSupervisorPermissionRuleScriptTest extends Specification {

    MigrationContext migrationContext = Mock()

    DatabaseHelper databaseHelper = Mock()

    ConfigurationHelper configurationHelper = Mock()

    FixProcessSupervisorPermissionRuleScript fixProcessSupervisorPermissionRuleScript

    def setup() {
        fixProcessSupervisorPermissionRuleScript = new FixProcessSupervisorPermissionRuleScript()
    }

    def "should retrieve new content"() {
        given:
        def expectedLineInContent = "   def map = mapper.readValue(apiCallContext.getBody(), Map.class)"
        def expectedNumberOfLines = 90

        when:
        def newContent = new String(fixProcessSupervisorPermissionRuleScript.newContent)

        then:
        newContent.contains(expectedLineInContent)
        newContent.split(System.getProperty("line.separator")).size() == expectedNumberOfLines
    }

    def "should call update configurationFileContent on all tenants and on tenant template"() {
        setup:
        migrationContext.configurationHelper >> configurationHelper
        migrationContext.databaseHelper >> databaseHelper
        databaseHelper.allTenants >> [[id: 1L, name: "tenant 1"],
                                      [id: 3L, name: "tenant 3"]]
        def expectedContent = fixProcessSupervisorPermissionRuleScript.newContent

        when:
        fixProcessSupervisorPermissionRuleScript.execute(migrationContext)


        then:
        1 * configurationHelper.updateConfigurationFileContent(resourceName, 0L, "TENANT_TEMPLATE_SECURITY_SCRIPTS", expectedContent)
        1 * configurationHelper.updateConfigurationFileContent(resourceName, 1L, "TENANT_SECURITY_SCRIPTS", expectedContent)
        1 * configurationHelper.updateConfigurationFileContent(resourceName, 3L, "TENANT_SECURITY_SCRIPTS", expectedContent)

    }
}
