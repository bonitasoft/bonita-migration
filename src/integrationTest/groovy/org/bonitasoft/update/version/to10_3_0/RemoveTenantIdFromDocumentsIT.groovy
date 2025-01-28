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

class RemoveTenantIdFromDocumentsIT extends AbstractTestTo10_3_0 {

    private RemoveTenantIdFromDocuments updateStep = new RemoveTenantIdFromDocuments()

    def "should remove tenantId from document tables"() {
        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            !hasColumnOnTable("document", "tenantId")
            hasPrimaryKeyOnTable("document", "pk_document")
            !hasForeignKeyOnTable("document", "fk_document_tenantId")

            !hasColumnOnTable("document_mapping", "tenantId")
            hasPrimaryKeyOnTable("document_mapping", "pk_document_mapping")
            !hasForeignKeyOnTable("document_mapping", "fk_document_mapping_tenantId")
            !hasForeignKeyOnTable("document_mapping", "fk_docmap_docid")
            hasForeignKeyOnTable("document_mapping", "fk_document_mapping_documentid")

            !hasColumnOnTable("arch_document_mapping", "tenantId")
            hasPrimaryKeyOnTable("arch_document_mapping", "pk_arch_document_mapping")
            !hasForeignKeyOnTable("arch_document_mapping", "fk_arch_document_mapping_tenantId")
            !hasForeignKeyOnTable("arch_document_mapping", "fk_ADocMap_tenId") // Oracle-specific
            !hasForeignKeyOnTable("arch_document_mapping", "fk_archdocmap_docid")
            hasForeignKeyOnTable("arch_document_mapping", "fk_arch_document_mapping_documentid")
        }
    }
}
