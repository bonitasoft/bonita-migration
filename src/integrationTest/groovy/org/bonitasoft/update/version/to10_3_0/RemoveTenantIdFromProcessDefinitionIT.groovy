/**
 * Copyright (C) 2024 Bonitasoft S.A.
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

class RemoveTenantIdFromProcessDefinitionIT extends AbstractTestTo10_3_0 {

    private RemoveTenantIdFromProcessDefinition updateStep = new RemoveTenantIdFromProcessDefinition()

    def "should remove tenantId from process_definition and related tables"() {
        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            !hasColumnOnTable("category", "tenantId")
            hasPrimaryKeyOnTable("category", "pk_category")
            !hasUniqueKeyOnTableWithColumns("category", "tenantId", "id")
            hasUniqueKeyOnTableWithNameAndColumns("category", "uk_category_name", "name")
            !hasForeignKeyOnTable("category", "fk_category_tenantId")

            !hasColumnOnTable("processcategorymapping", "tenantId")
            hasPrimaryKeyOnTable("processcategorymapping", "pk_processcategorymapping")
            !hasUniqueKeyOnTableWithColumns("processcategorymapping", "tenantid", "categoryid", "processid")
            hasUniqueKeyOnTableWithNameAndColumns("processcategorymapping", "uk_processcategorymapping_categoryid_processid", "categoryid", "processid")
            !hasForeignKeyOnTable("processcategorymapping", "fk_processcategorymapping_tenantId")
            !hasForeignKeyOnTable("processcategorymapping", "fk_procCatMap_tenId") // Oracle-specific

            !hasColumnOnTable("process_content", "tenantId")
            hasPrimaryKeyOnTable("process_content", "pk_process_content")

            !hasColumnOnTable("process_definition", "tenantId")
            !hasColumnOnTable("process_definition", "content_tenantid")
            hasPrimaryKeyOnTable("process_definition", "pk_process_definition")
            !hasUniqueKeyOnTableWithColumns("process_definition", "tenantId", "name", "version")
            hasUniqueKeyOnTableWithNameAndColumns("process_definition", "uk_process_definition_name_version", "name", "version")
            !hasForeignKeyOnTable("process_definition", "fk_process_definition_tenantId")
            !hasForeignKeyOnTable("process_definition", "fk_process_definition_content")
            hasForeignKeyOnTable("process_definition", "fk_process_definition_content_id")

            !hasColumnOnTable("processsupervisor", "tenantId")
            hasPrimaryKeyOnTable("processsupervisor", "pk_processsupervisor")
            !hasUniqueKeyOnTableWithColumns("processsupervisor", "tenantId", "name")
            hasUniqueKeyOnTableWithNameAndColumns("processsupervisor", "uk_processsupervisor_processdefid_userid_groupid_roleid", "processDefId", "userId", "groupId", "roleId")
            !hasForeignKeyOnTable("processsupervisor", "fk_processsupervisor_tenantId")

            !hasColumnOnTable("proc_parameter", "tenantId")
            hasPrimaryKeyOnTable("proc_parameter", "pk_proc_parameter")
        }
    }
}
