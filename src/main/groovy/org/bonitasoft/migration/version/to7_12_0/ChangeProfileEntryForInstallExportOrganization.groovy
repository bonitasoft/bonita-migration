/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_12_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class ChangeProfileEntryForInstallExportOrganization extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        renameInstallExportOrganizationPage(context)
    }

    private static renameInstallExportOrganizationPage(MigrationContext context) {
        def newName = 'custompage_adminInstallExportOrganizationBonita'
        context.logger.info("Updating custompage_installExportOrganizationBonita => $newName for profileentry")
        context.databaseHelper.executeUpdate("UPDATE profileentry SET PAGE = '$newName' WHERE PAGE = 'custompage_installExportOrganizationBonita'")
    }

    @Override
    String getDescription() {
        return "Rename profileentry page token for install Export Organization"
    }
}
