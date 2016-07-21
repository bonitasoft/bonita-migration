package org.bonitasoft.migration.versions.v6_5_4_to_v7_0_0

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.bonitasoft.migration.versions.v7_0_0.PageTableMigration

import static org.assertj.core.api.Assertions.assertThat
import static org.bonitasoft.migration.DBUnitHelper.*

/**
 * @author Laurent Leseigneur
 */
class PageTableIT extends GroovyTestCase {

    Sql sql

    @Override
    void setUp() {
        sql = createSqlConnection();
        dropTestTables()
        createTables(sql, "6_5_4", "page");
    }


    @Override
    void tearDown() {
        dropTestTables()
        sql.close()
    }

    private void dropTestTables() {
        def String[] strings = [
                "business_app_page",
                "page",
                "tenant"
        ]
        dropTables(sql, strings)
    }

    void test_migrate_on_oracle_should_update_unique_index_on_page_table() throws Exception {

        if ("oracle".equals(dbVendor())) {
            //given
            JsonBuilder builderBefore = new JsonBuilder(checkOracleMetaData())
            def expectedBefore = new JsonSlurper().parseText("""{"column_1" : "TENANTID", "column_2" : "NAME" }""")
            assertThat(builderBefore.toPrettyString()).as("should have unique index with 2 keys key").isEqualTo(new JsonBuilder(expectedBefore).toPrettyString())

            //when
            new PageTableMigration(sql, dbVendor()).migrate()

            //then
            checkOracleMetaData()
            JsonBuilder builderAfter = new JsonBuilder(checkOracleMetaData())
            def expectedAfter = new JsonSlurper().parseText("""{"column_1":"TENANTID", "column_2": "NAME" ,  "column_3": "PROCESSDEFINITIONID" }""")
            assertThat(builderAfter.toPrettyString()).as("should have modified index with process definition id ").isEqualTo(new JsonBuilder(expectedAfter).toPrettyString())
        }


    }

    def checkOracleMetaData() {
        def meta = [:]
        sql.eachRow("""
                    SELECT
                        ic.TABLE_NAME,
                        ic.INDEX_NAME,
                        ic.COLUMN_NAME,
                        ic.COLUMN_POSITION
                    FROM
                        USER_IND_COLUMNS ic
                    WHERE
                        UPPER( ic.TABLE_NAME ) = 'PAGE'
                        AND UPPER( ic.INDEX_NAME ) = 'UK_PAGE'
                    ORDER BY
                        ic.COLUMN_POSITION """) {
            meta << ["column_${it['COLUMN_POSITION']}": "${it['COLUMN_NAME']}"]
        }
        meta
    }

}
