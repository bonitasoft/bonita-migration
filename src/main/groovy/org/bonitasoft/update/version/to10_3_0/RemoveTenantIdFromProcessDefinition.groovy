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

/**
 * Remove tenantId from tables:
 * process_definition
 * process_content
 * category
 * processcategorymapping
 * processsupervisor
 * proc_parameter
 *
 * @author Emmanuel Duchastenier
 */
class RemoveTenantIdFromProcessDefinition extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {

            // drop FK first:
            dropForeignKey("category", "fk_category_tenantId")
            dropForeignKey("processcategorymapping", "fk_catmapping_catid")
            dropForeignKey("processcategorymapping", "fk_processcategorymapping_categoryid") // to make step reentrant
            dropForeignKey("processcategorymapping", dbVendor == DBVendor.ORACLE ? "fk_procCatMap_tenId" : "fk_processcategorymapping_tenantId")
            dropForeignKey("process_definition", "fk_process_definition_tenantId")
            dropForeignKey("process_definition", "fk_process_definition_content")
            dropForeignKey("process_definition", "fk_process_definition_content_id") // to make step reentrant
            dropForeignKey("processsupervisor", "fk_processsupervisor_tenantId")

            // drop indexes that are no longer needed (because they match the same columns as a unique or primary key):
            // None for those objects.

            // recreate PK:
            recreatePrimaryKey("category")
            recreatePrimaryKey("processcategorymapping")
            recreatePrimaryKey("process_content")
            recreatePrimaryKey("process_definition")
            recreatePrimaryKey("processsupervisor")
            recreatePrimaryKey("proc_parameter")

            // recreate UK:
            dropUniqueKeyFromColumns("category", "tenantId", "name")
            createUniqueKey("category", "uk_category_name", "name")
            dropUniqueKeyFromColumns("processcategorymapping", "tenantid", "categoryid", "processid")
            createUniqueKey("processcategorymapping", "uk_processcategorymapping_categoryid_processid", "categoryid", "processid")
            dropUniqueKeyFromColumns("process_definition", "tenantId", "name")
            createUniqueKey("process_definition", "uk_process_definition_name_version", "name", "version")
            dropUniqueKeyFromColumns("processsupervisor", "tenantid", "processDefId", "userId", "groupId", "roleId")
            createUniqueKey("processsupervisor", "uk_processsupervisor_processdefid_userid_groupid_roleid", "processDefId", "userId", "groupId", "roleId")

            // recreate FK:
            createForeignKey("processcategorymapping", "fk_processcategorymapping_categoryid", "category", ["categoryid"], ["id"], true)
            createForeignKey("process_definition", "fk_process_definition_content_id", "process_content", ["content_id"], ["id"], false)

            // drop the columns:
            dropColumnIfExists("category", "tenantId")
            dropColumnIfExists("processcategorymapping", "tenantId")
            dropColumnIfExists("process_content", "tenantId")
            dropColumnIfExists("process_definition", "tenantId")
            dropColumnIfExists("process_definition", "content_tenantid") // JOIN column
            dropColumnIfExists("processsupervisor", "tenantId")
            dropColumnIfExists("proc_parameter", "tenantId")
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from process-related tables: process_definition, process_content, category, processcategorymapping, processsupervisor, proc_parameter"
    }
}
