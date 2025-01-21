/**
 * Copyright (C) 2025 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.update.version.to10_3_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class RemoveTenantIdFromDependencyTables extends UpdateStep {
    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            // drop FK first:
            dropForeignKey("dependency", "fk_dependency_tenantId")
            dropForeignKey("dependencymapping", "fk_dependencymapping_tenantId")
            dropForeignKey("dependencymapping", "fk_depmapping_depid")
            dropForeignKey("pdependencymapping", "fk_pdepmapping_depid")

            // drop indexes that are no longer needed (because they match the same columns as a unique or primary key):
            dropIndexIfExists("dependency", "idx_dependency_name")
            dropIndexIfExists("pdependency", "idx_pdependency_name")

            // recreate PK:
            dropPrimaryKey("dependency")
            createPrimaryKey("dependency", "id")
            dropPrimaryKey("dependencymapping")
            createPrimaryKey("dependencymapping", "id")
            // renaming PK from platform dependency table to match the new naming convention:
            dropPrimaryKey("pdependency")
            createPrimaryKey("pdependency", "id")
            dropPrimaryKey("pdependencymapping")
            createPrimaryKey("pdependencymapping", "id")

            // recreate UK:
            dropUniqueKeyFromColumns("dependency", "tenantId", "name")
            createUniqueKey("dependency", "uk_dependency_name", "name")
            dropUniqueKeyFromColumns("dependencymapping", "tenantid", "dependencyid", "artifactid", "artifacttype")
            createUniqueKey("dependencymapping", "uk_dependencymapping_dependencyid_artifactid_artifacttype",
                    "dependencyid", "artifactid", "artifacttype")
            // renaming FK from platform dependency table to match the new naming convention:
            dropUniqueKeyFromColumns("pdependency", "name")
            createUniqueKey("pdependency", "uk_pdependency_name", "name")
            dropUniqueKeyFromColumns("pdependencymapping", "dependencyid", "artifactid", "artifacttype")
            createUniqueKey("pdependencymapping", "uk_pdependencymapping_dependencyid_artifactid_artifacttype",
                    "dependencyid", "artifactid", "artifacttype")

            // recreate FK:
            createForeignKey("dependencymapping", "fk_dependencymapping_dependencyid",
                    "dependency", ["dependencyid"], ["id"], true)
            createForeignKey("pdependencymapping", "fk_pdependencymapping_dependencyid",
                    "pdependency", ["dependencyid"], ["id"], true)

            // drop the columns:
            dropColumnIfExists("dependency", "tenantId")
            dropColumnIfExists("dependencymapping", "tenantId")
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from dependency tables: dependency, dependency_mapping"
    }
}
