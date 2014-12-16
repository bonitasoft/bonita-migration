package org.bonitasoft.migration.versions.v6_4_0_to_6_4_1

import groovy.sql.Sql
import org.dbunit.JdbcDatabaseTester

import static org.bonitasoft.migration.DBUnitHelper.*
import static org.assertj.core.api.Assertions.*

/**
 * @author Emmanuel Duchastenier
 */
class MigrateDateDataInstancesFromWrongXMLObjectIT extends GroovyTestCase {

    Sql sql
    JdbcDatabaseTester tester

    @Override
    void setUp() {
        sql = createSqlConnection();
        tester = createTester()

        createTables(sql, "data_instance")
    }


    @Override
    void tearDown() {
        tester.onTearDown();

        def String[] tables = ["data_instance"]
        dropTables(sql, tables)
    }

    void test_migrate_should_move_data_instances_from_xmlobject_to_date_column() throws Exception {
        //given
        def Long dateTime = 1418660268855;
        def xmlDate = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><date>2014-12-15 16:17:48.855 UTC</date>"; // equivalent to 1418660268855 in UTC and English locale (XStream defaults)

        sql.executeInsert("INSERT INTO data_instance (tenantid, id, LONGVALUE, CLOBVALUE, DISCRIMINANT) VALUES (1, 14, null, '" + xmlDate + "', 'SXMLObjectDataInstanceImpl')")
        def dateDataInstanceMigration = new MigrateDateDataInstancesFromWrongXMLObject(sql, dbVendor())

        //when
        dateDataInstanceMigration.migrate()

        //then
        def row = sql.firstRow("SELECT LONGVALUE, CLOBVALUE, DISCRIMINANT FROM data_instance where tenantid = 1 and ID = 14");
        def Long dateAsLong = row.getProperty("LONGVALUE")
        def String dateAsClob = row.getProperty("CLOBVALUE")
        def String dataType = row.getProperty("DISCRIMINANT")
        assertThat(dateAsLong).isEqualTo(dateTime)
        assertThat(dateAsClob).isNull()
        assertThat(dataType).isEqualTo("SDateDataInstanceImpl")
    }
}
