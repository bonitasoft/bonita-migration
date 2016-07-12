package org.bonitasoft.migration.versions.v6_4_0_to_6_4_1

import groovy.sql.Sql
import org.assertj.core.api.Assertions

import static org.bonitasoft.migration.DBUnitHelper.*

/**
 * @author Elias Ricken de Medeiros
 */
class AddArchDocumentMappingIndexIT extends GroovyTestCase {


    Sql sql

    @Override
    void setUp() {
        sql = createSqlConnection()
        dropTestTables()
        createTables(sql, "documents")

    }


    @Override
    void tearDown() {
        dropTestTables()
    }

    private void dropTestTables() {
        def String[] strings = [
                "document_content",
                "document_mapping",
                "arch_document_mapping",
                "sequence",
                "tenant"
        ]
        dropTables(sql, strings)
    }

    void test_migrate_should_add_index_using_columns_processInstanceId_and_tenantId() throws Exception {
        //given
        def addArchDocumentMappingIndex = new AddArchDocumentMappingIndex(sql, dbVendor())

        //when
        def addedIndex = addArchDocumentMappingIndex.migrate()

        //then
        Assertions.assertThat(addedIndex).isEqualTo("CREATE INDEX idx_a_doc_mp_pr_id ON arch_document_mapping (processinstanceid, tenantid)")
    }

}
