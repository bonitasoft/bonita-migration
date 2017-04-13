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

package org.bonitasoft.migration.version.to7_4_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Laurent Leseigneur
 */
class UpdatePermissionMappingProperties extends MigrationStep {

    public static String configFile = "resources-permissions-mapping.properties"

    @Override
    def execute(MigrationContext context) {
        context.databaseHelper.appendToAllConfigurationFilesIfPropertyIsMissing(configFile, "POST|portal/custom-page/API/formFileUpload", "[form_file_upload]")
        context.databaseHelper.appendToAllConfigurationFilesIfPropertyIsMissing(configFile, "GET|portal/custom-page/API/avatars", "[avatars]")
        context.databaseHelper.appendToAllConfigurationFilesIfPropertyIsMissing(configFile, "GET|portal/custom-page/API/documentDownload", "[download_document]")
        context.databaseHelper.appendToAllConfigurationFilesIfPropertyIsMissing(configFile, "GET|API/formsDocumentImage", "[download_document]")
        context.databaseHelper.appendToAllConfigurationFilesIfPropertyIsMissing(configFile, "GET|portal/custom-page/API/formsDocumentImage", "[download_document]")
        context.databaseHelper.appendToAllConfigurationFilesIfPropertyIsMissing(configFile, "GET|portal/formsDocumentImage", "[download_document]")
        context.databaseHelper.appendToAllConfigurationFilesIfPropertyIsMissing(configFile, "GET|portal/custom-page/API/formsDocumentDownload", "[download_document]")
        context.databaseHelper.appendToAllConfigurationFilesIfPropertyIsMissing(configFile, "GET|portal/custom-page/API/downloadDocument", "[download_document]")
    }

    @Override
    String getDescription() {
        return "update required permissions required by new REST API added in 7.4.0"
    }


}
