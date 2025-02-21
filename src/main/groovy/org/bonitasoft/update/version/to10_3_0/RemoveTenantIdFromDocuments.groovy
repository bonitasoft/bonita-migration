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

import static org.bonitasoft.update.core.UpdateStep.DBVendor.ORACLE
/**
 * Remove tenantId from tables 'documents', 'document_mapping' and 'arch_document_mapping'
 */
class RemoveTenantIdFromDocuments extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {dbHelper ->
            // drop FK first:
            dropForeignKey("document", "fk_document_tenantId")
            if (dbVendor != ORACLE) {
                dropForeignKey("document_mapping", "fk_document_mapping_tenantId")
            }
            dropForeignKey("document_mapping", "fk_docmap_docid")
            dropForeignKey("document_mapping", "fk_document_mapping_documentid") // to make step reentrant
            dropForeignKey("arch_document_mapping", dbVendor == ORACLE ? "fk_ADocMap_tenId" : "fk_arch_document_mapping_tenantId")
            dropForeignKey("arch_document_mapping", "fk_archdocmap_docid")
            dropForeignKey("arch_document_mapping", "fk_arch_document_mapping_documentid") // to make step reentrant

            // drop indexes that are no longer needed (because they match the same columns as a unique or primary key):
            // no index on those tables to drop

            // recreate PK:
            recreatePrimaryKey("document")
            recreatePrimaryKey("document_mapping")
            recreatePrimaryKey("arch_document_mapping")

            // recreate UK:
            // No UK for those tables

            // recreate FK:
            createForeignKey("document_mapping", "fk_document_mapping_documentid", "document", ["documentId"], ["id"], true)
            createForeignKey("arch_document_mapping", "fk_arch_document_mapping_documentid", "document", ["documentId"], ["id"], true)

            // drop the columns:
            dropColumnIfExists("document", "tenantId")
            dropColumnIfExists("document_mapping", "tenantId")
            dropColumnIfExists("arch_document_mapping", "tenantId")
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from 'documents', 'document_mapping' and 'arch_document_mapping' tables"
    }
}
