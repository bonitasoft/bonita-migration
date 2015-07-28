package org.bonitasoft.migration.versions.v6_2_6_to_6_3_0
import groovy.sql.Sql
import org.dbunit.JdbcDatabaseTester

import static org.assertj.core.api.Assertions.assertThat
import static org.bonitasoft.migration.DBUnitHelper.*

class IndexExistsCheckerIT extends GroovyTestCase {

    final static String DBVENDOR
    final static DROP_TABLE_6_2_6
    final static CREATE_BAD_INDEX

    static{
        DBVENDOR = System.getProperty("db.vendor");

        DROP_TABLE_6_2_6 = IndexExistsCheckerIT.class.getClassLoader().getResource("sql/v6_2_6/${DBVENDOR}-drop-tables.sql");
        CREATE_BAD_INDEX = IndexExistsCheckerIT.class.getClassLoader().getResource("sql/v6_2_6/${DBVENDOR}-create-index.sql");
    }

    JdbcDatabaseTester tester
    Sql sql

    @Override
    void setUp() {
        println ("setUp")
        sql = createSqlConnection();
        tester = createTester()

        println ("setUp: create tables")
        createTables(sql, "6_2_6", "create-tables");
    }

    @Override
    void tearDown() {
        println ("tearDown: drop tables")
        tester.onTearDown()

        sql.execute(DROP_TABLE_6_2_6.text);
    }


    void test_can_migrate_a_database_without_index() {
        //given
        def indexFound=false

        //when
        def feature = new File("build/dist/versions/6.3.0/Database/000_checkDatabase/${DBVENDOR}.sql")

        //then
        sql.eachRow(feature.text) {row ->
            indexFound=true
            println "index:${row.indexname} found on table:${row.tablename}"
        }
        assertThat(indexFound).isFalse();

    }


    void test_migrate_a_database_with_bad_index_should_fail() {
        //given
        def indexFound=false

        sql.execute(CREATE_BAD_INDEX.text);

        //when
        def feature = new File("build/dist/versions/6.3.0/Database/000_checkDatabase/${DBVENDOR}.sql")


        //then
        sql.eachRow(feature.text) {row ->
            indexFound=true
            println "index:${row.indexname} found on table:${row.tablename}"
        }
        assertThat(indexFound).isTrue();
    }
}
