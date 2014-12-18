package org.bonitasoft.migration.versions.v6_4_0_to_6_4_1

import groovy.sql.Sql
import org.bonitasoft.migration.core.DatabaseMigrationStep

/**
 * @author Elias Ricken de Medeiros
 */
class AddArchDocumentMappingIndex extends DatabaseMigrationStep {

    AddArchDocumentMappingIndex(final Sql sql, final String dbVendor) {
        super(sql, dbVendor)
    }

    @Override
    def migrate() {
        return addIndex("arch_document_mapping", "idx_a_doc_mp_pr_id", "processinstanceid", "tenantid")
    }
}
