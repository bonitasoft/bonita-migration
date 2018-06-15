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
package org.bonitasoft.migration.version.to7_7_1

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class RenameConnectorDependencyFilename extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        renameFilenameColumnValueForConnectorDependencies(context)
    }

    private static renameFilenameColumnValueForConnectorDependencies(MigrationContext context) {
        context.sql.eachRow("""
            SELECT
                dm.artifactid, d.tenantid, d.id, d.filename
            FROM
                dependencymapping dm,
                dependency d
            WHERE
                dm.artifacttype = 'PROCESS'
                AND dm.dependencyid = d.id
                AND dm.tenantid = d.tenantid
                AND d.filename LIKE concat(dm.artifactid, '_%')
       """) { row ->
            def processId = row[0] as String
            long tenantId = row[1] as long
            long dependencyId = row[2] as long
            def filename = row[3] as String
            String newName = filename.replace(processId + "_", "")
            context.logger.info("Updating connector dependency filename $filename => $newName for process $processId")
            context.databaseHelper.executeUpdate("UPDATE dependency SET filename = '$newName' WHERE tenantId = $tenantId AND id = $dependencyId")
        }
    }

    @Override
    String getDescription() {
        return "Rename connector JAR dependency filename at PROCESS-level"
    }
}
