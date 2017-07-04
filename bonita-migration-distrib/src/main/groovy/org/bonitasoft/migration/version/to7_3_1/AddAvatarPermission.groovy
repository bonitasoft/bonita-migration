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

class AddAvatarPermission extends MigrationStep {

    public static final String RESOURCE_PERMISSIONS_MAPPING_FILE_NAME = "resources-permissions-mapping.properties"
    public static final String COMPOUND_PERMISSIONS_MAPPING_FILE_NAME = "compound-permissions-mapping.properties"

    @Override
    def execute(MigrationContext context) {
        migrateResourcePermissionsFiles(context)
        migrateCompoundPermissionsMappingFiles(context)
    }

    def migrateResourcePermissionsFiles(MigrationContext context) {
        context.configurationHelper.appendToAllConfigurationFilesIfPropertyIsMissing(RESOURCE_PERMISSIONS_MAPPING_FILE_NAME, "GET|API/avatars", "[avatars]")
    }

    def migrateCompoundPermissionsMappingFiles(MigrationContext context) {
        context.configurationHelper.updateAllConfigurationFilesIfPermissionValueIsMissing(COMPOUND_PERMISSIONS_MAPPING_FILE_NAME, compoundPropertiesKeysToMigrate(), "avatars")
    }

    private List<String> compoundPropertiesKeysToMigrate() {
        // This list of keys is taken from the compound-permissions-mapping.properties file in Bonita version 7.3.1
        // It is the same for all versions up to 7.5.2
        [
                "custompage_htmlexample"
                , "pagelisting"
                , "importexportorganization"
                , "tenantMaintenance"
                , "processlistinguser"
                , "processlistingpm"
                , "tasklistingadmin"
                , "userlistingadmin"
                , "rolelistingadmin"
                , "tasklistinguser"
                , "profilelisting"
                , "caselistingadmin"
                , "thememoredetailsadminext"
                , "processlistingadmin"
                , "tasklistingpm"
                , "caselistingpm"
                , "applicationslistingadmin"
                , "businessdatamodelimport"
                , "grouplistingadmin"
                , "caselistinguser"
                , "reportlistingadminext"
                , "custompage_groovyexample"
                , "custompage_home"
                , "custompage_defaultlayout"
                , "custompage_apiExtensionViewer"
                , "custompage_tasklist"
        ]
    }

    @Override
    String getDescription() {
        return "Add avatar permission configuration in files in database"
    }
}
