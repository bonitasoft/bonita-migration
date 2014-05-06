package org.bonitasoft.migration.versions.v6_2_6_to_6_3_0
;

import static org.assertj.core.api.Assertions.assertThat
import static org.junit.Assert.*
import groovy.sql.Sql
import groovy.xml.StreamingMarkupBuilder

import org.dbunit.JdbcDatabaseTester
import org.dbunit.assertion.DbUnitAssert
import org.dbunit.dataset.xml.FlatXmlDataSet
import org.junit.Test;


class IndexExistsCheckerIT extends GroovyTestCase {

    final static String DBVENDOR
    final static CREATE_TABLE_6_2_6
    final static DROP_TABLE_6_2_6
    final static CREATE_BAD_INDEX

    static{
        DBVENDOR = System.getProperty("db.vendor");

        CREATE_TABLE_6_2_6 = IndexExistsCheckerIT.class.getClassLoader().getResource("sql/v6_2_6/${DBVENDOR}-create-tables.sql");
        DROP_TABLE_6_2_6 = IndexExistsCheckerIT.class.getClassLoader().getResource("sql/v6_2_6/${DBVENDOR}-drop-tables.sql");
        CREATE_BAD_INDEX = IndexExistsCheckerIT.class.getClassLoader().getResource("sql/v6_2_6/${DBVENDOR}-create-index.sql");
    }

    JdbcDatabaseTester tester
    Sql sql

    @Override
    void setUp() {
        println ("setUp")
        String driverClass =  System.getProperty("jdbc.driverClass")

        def config = [
            System.getProperty("jdbc.url"),
            System.getProperty("jdbc.user"),
            System.getProperty("jdbc.password")
        ]
        sql = Sql.newInstance(*config, driverClass);
        tester = new JdbcDatabaseTester(driverClass, *config)

        println ("setUp: create tables")
        sql.execute(CREATE_TABLE_6_2_6.text);
    }

    @Override
    void tearDown() {
        println ("tearDown: drop tables")
        sql.execute(DROP_TABLE_6_2_6.text);
        tester.onTearDown()
    }


    void test_can_migrate_a_database_without_index() {
        //given
        def indexFound=false

        //when
        def feature = new File("build/dist/versions/6.2.6-6.3.0/Database/000_checkDatabase/${DBVENDOR}.sql")

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
        def feature = new File("build/dist/versions/6.2.6-6.3.0/Database/000_checkDatabase/${DBVENDOR}.sql")


        //then
        sql.eachRow(feature.text) {row ->
            indexFound=true
            println "index:${row.indexname} found on table:${row.tablename}"
        }
        assertThat(indexFound).isTrue();
    }
}
