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

class RemoveTenantIdFromDependencyTablesIT extends AbstractTestTo10_3_0 {

    private RemoveTenantIdFromDependencyTables updateStep = new RemoveTenantIdFromDependencyTables()

    def "should remove tenantId from dependency tables"() {
        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            !hasColumnOnTable("dependency", "tenantid")
            hasPrimaryKeyOnTable("dependency", "pk_dependency")
            hasUniqueKeyOnTableWithNameAndColumns("dependency", "uk_dependency_name", "name")
            !hasUniqueKeyOnTableWithColumns("dependency", "tenantid", "name")
            !hasUniqueKeyOnTable("dependency", "UK_Dependency") // ORACLE
            !hasIndexOnTable("dependency", "idx_dependency_name")
            !hasForeignKeyOnTable("dependency", "fk_dependency_tenantId")

            !hasColumnOnTable("dependencymapping", "tenantid")
            hasPrimaryKeyOnTable("dependencymapping", "pk_dependencymapping")
            hasUniqueKeyOnTableWithNameAndColumns("dependencymapping",
                    "uk_dependencymapping_dependencyid_artifactid_artifacttype",
                    "dependencyid", "artifactid", "artifacttype")
            !hasUniqueKeyOnTableWithColumns("dependencymapping",
                    "tenantid", "dependencyid", "artifactid", "artifacttype")
            !hasUniqueKeyOnTable("dependencymapping", "UK_Dependency_Mapping") // ORACLE
            !hasForeignKeyOnTable("dependencymapping", "fk_dependencymapping_tenantId")
            hasForeignKeyOnTable("dependencymapping", "fk_dependencymapping_dependencyid")

            hasPrimaryKeyOnTable("pdependency", "pk_pdependency")
            hasUniqueKeyOnTableWithNameAndColumns("pdependency", "uk_pdependency_name", "name")
            !hasIndexOnTable("pdependency", "idx_pdependency_name")

            hasPrimaryKeyOnTable("pdependencymapping", "pk_pdependencymapping")
            hasUniqueKeyOnTableWithNameAndColumns("pdependencymapping",
                    "uk_pdependencymapping_dependencyid_artifactid_artifacttype",
                    "dependencyid", "artifactid", "artifacttype")
            !hasUniqueKeyOnTable("pdependencymapping", "UK_PDependency_Mapping") // ORACLE
            hasForeignKeyOnTable("pdependencymapping", "fk_pdependencymapping_dependencyid")
        }
    }
}
