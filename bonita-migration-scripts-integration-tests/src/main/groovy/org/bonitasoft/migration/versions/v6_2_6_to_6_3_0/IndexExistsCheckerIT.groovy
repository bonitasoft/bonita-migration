package org.bonitasoft.migration.versions.v6_2_6_to_6_3_0

import groovy.sql.Sql

import static org.assertj.core.api.Assertions.assertThat
import static org.bonitasoft.migration.DBUnitHelper.*

class IndexExistsCheckerIT extends GroovyTestCase {

    final static String DBVENDOR
    final static CREATE_BAD_INDEX

    static {
        DBVENDOR = System.getProperty("db.vendor");
        CREATE_BAD_INDEX = IndexExistsCheckerIT.class.getClassLoader().getResource("sql/v6_2_6/${DBVENDOR}-create-index.sql");
    }

    Sql sql

    @Override
    void setUp() {
        println("setUp")
        sql = createSqlConnection();
        dropTestTables()
        createTables(sql, "6_2_6", "create-tables");
    }

    @Override
    void tearDown() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dropTables(sql, ["arch_process_instance",
                         "arch_flownode_instance",
                         "arch_transition_instance",
                         "arch_connector_instance",
                         "arch_data_instance",
                         "arch_data_mapping",
                         "token"] as String[])
    }


    void test_can_migrate_a_database_without_index() {
        //given
        def indexFound = false

        //when
        def feature = new File("build/dist/versions/6.3.0/Database/000_checkDatabase/${DBVENDOR}.sql")

        //then
        sql.eachRow(feature.text) { row ->
            indexFound = true
            println "index:${row.indexname} found on table:${row.tablename}"
        }
        assertThat(indexFound).isFalse();

    }


    void test_migrate_a_database_with_bad_index_should_fail() {
        //given
        def indexFound = false

        sql.execute(CREATE_BAD_INDEX.text);

        //when
        def feature = new File("build/dist/versions/6.3.0/Database/000_checkDatabase/${DBVENDOR}.sql")

        //then
        sql.eachRow(feature.text) { row ->
            indexFound = true
            println "index:${row.indexname} found on table:${row.tablename}"
        }
        assertThat(indexFound).isTrue();
    }
}
