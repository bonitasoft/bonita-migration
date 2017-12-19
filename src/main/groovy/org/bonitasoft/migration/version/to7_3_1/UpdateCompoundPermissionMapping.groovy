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

package org.bonitasoft.migration.version.to7_3_1

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Laurent Leseigneur
 */
class UpdateCompoundPermissionMapping extends MigrationStep {

    public static String resourceName = "compound-permissions-mapping.properties"

    def key = "caselistingpm"
    
    def caseListingPmPermissions = "[case_delete, process_visualization, process_categories, process_comment, document_visualization, process_actor_mapping_visualization, organization_visualization, case_visualization, task_management, flownode_management, connector_visualization, tenant_platform_visualization, flownode_visualization, task_visualization, download_document, form_visualization, bdm_visualization]"

    @Override
    def execute(MigrationContext context) {
        context.configurationHelper.updateKeyInAllPropertyFiles(resourceName, key, caseListingPmPermissions, null)
    }

    @Override
    String getDescription() {
        return "update compound-permissions-mapping.properties if caselistingpm is missing 'case_delete' permission"
    }


}
