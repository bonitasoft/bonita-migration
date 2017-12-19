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
package org.bonitasoft.migration.version.to7_4_2

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import org.bonitasoft.migration.core.database.ConfigurationHelper
import org.bonitasoft.migration.core.database.DatabaseHelper

/**
 * @author Emmanuel Duchastenier
 */
class AddManagerInvolvedConfiguration extends MigrationStep {

    DatabaseHelper databaHelper
    ConfigurationHelper configurationHelper

    @Override
    execute(MigrationContext context) {
        databaHelper = context.databaseHelper
        configurationHelper = context.configurationHelper
        migrateFormConfigPropertiesFile()
        addNewCommentedBeans(context)
        migrateEngineTenantCustomPropertiesFile()
    }

    def addNewCommentedBeans(MigrationContext context) {
        String configFile = 'bonita-tenants-custom.xml'
        String toAppend = this.getClass().getResourceAsStream("/version/to_7_4_2/bonita-tenants-custom.txt").getText("UTF-8")

        // For each tenant...
        context.sql.eachRow("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=$configFile
                """) {
            def tenantId = it.tenant_id as long
            def contentType = it.content_type as String
            String content = databaHelper.getBlobContentAsString(it["resource_content"])

            content = content.replace('</beans>', "$toAppend\n</beans>")
            update(configFile, tenantId, contentType, content.bytes)
        }
    }

    def migrateFormConfigPropertiesFile() {
        configurationHelper.appendToAllConfigurationFilesWithName("form-config.properties", "form.authorizations.manager.allowed     false")
    }

    def migrateEngineTenantCustomPropertiesFile() {
        configurationHelper.appendToAllConfigurationFilesWithName("bonita-tenant-community-custom.properties", """
# to restore pre-7.3.0 behavior (where manager of user involved in process instance could access Case Overview), use this implementation below instead:
#bonita.tenant.authorization.rule.mapping=managerInvolvedAuthorizationRuleMappingImpl
""")
    }

    private update(String fileName, long tenantId, String type, byte[] content) {
        configurationHelper.updateConfigurationFileContent(fileName, tenantId, type, content)
    }

    @Override
    String getDescription() {
        return "Add isManagerOfUserInvolved configuration in files in database"
    }

}
