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
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Laurent Leseigneur
 */
class FixProcessSupervisorPermissionRuleScript extends MigrationStep {

    public static String resourceName = "ProcessSupervisorPermissionRule.groovy"

    @Override
    def execute(MigrationContext context) {
        def newScriptContent = getNewContent()

        context.configurationHelper.updateConfigurationFileContent(resourceName, 0L, 'TENANT_TEMPLATE_SECURITY_SCRIPTS',
                newScriptContent)

        context.databaseHelper.allTenants.each { tenant ->
            context.configurationHelper.updateConfigurationFileContent(resourceName, tenant.id as long,
                    'TENANT_SECURITY_SCRIPTS', newScriptContent)
        }
    }

    def getNewContent() {
        this.getClass().getResourceAsStream("/version/to_7_3_3/${resourceName}.txt").bytes
    }


    @Override
    String getDescription() {
        return "update ProcessSupervisorPermissionRule.groovy security script"
    }
}
