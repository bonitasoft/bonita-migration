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

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class ChangeProfileEntryForOrganizationImport extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        updateNameOfProfilesEntries(context)
        updateDescriptionOfProfilesEntries(context)
        renamePageValueToUseNewOrganizationImportPage(context)
    }
    
    private static updateNameOfProfilesEntries(MigrationContext context) {
        def newProfileEntryName = 'Install / Export'
        context.logger.info("Updating default name for all profile entries")
        context.databaseHelper.executeUpdate("UPDATE profileentry SET NAME = '$newProfileEntryName' WHERE NAME = 'Import / Export' AND PAGE = 'importexportorganization'")
    }
    
    private static updateDescriptionOfProfilesEntries(MigrationContext context) {
        def newProfileEntryDescription = 'Install / Export a complete organization'
        context.logger.info("Updating default description for all profile entries")
        context.databaseHelper.executeUpdate("UPDATE profileentry SET DESCRIPTION = '$newProfileEntryDescription' WHERE DESCRIPTION = 'Import / Export an final organization' AND PAGE = 'importexportorganization'")
    }
    
    private static renamePageValueToUseNewOrganizationImportPage(MigrationContext context) {
        def newPageName = 'custompage_installExportOrganizationBonita'
        context.logger.info("Updating importexportorganization => $newPageName for all profile entries")
        context.databaseHelper.executeUpdate("UPDATE profileentry SET PAGE = '$newPageName' WHERE PAGE = 'importexportorganization'")
    }

    @Override
    String getDescription() {
        return "Update profile entries for install export organization page"
    }
}
