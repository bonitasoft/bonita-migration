package org.bonitasoft.migration.versions.v6_4_0_to_6_4_1

import groovy.sql.Sql

import static org.assertj.core.api.Assertions.assertThat
import static org.bonitasoft.migration.DBUnitHelper.*

/**
 * @author Emmanuel Duchastenier
 */
class MigrateDateDataInstancesFromWrongXMLObjectIT extends GroovyTestCase {

    public static final int SAMPLE_SIZE = 1000
    Sql sql

    @Override
    void setUp() {
        sql = createSqlConnection();
        droptesttables()
        createTables(sql, "data_instance")
    }


    @Override
    void tearDown() {
        droptesttables()
    }

    private void droptesttables() {
        def String[] tables = [
                "data_instance",
                "arch_data_instance"
        ]
        dropTables(sql, tables)
    }

    void test_migrate_should_move_data_instances_from_xmlobject_to_date_column() throws Exception {
        //given
        def Long dateTime = 1418660268855;
        def xmlDate = """<?xml version="1.0" encoding="UTF-8" ?><date>2014-12-15 16:17:48.855 UTC</date>""" as String;
        // equivalent to 1418660268855 in UTC and English locale (XStream defaults)
        def xmlNullDate = """<?xml version="1.0" encoding="UTF-8" ?><null></null>""" as String;
        def counter = 0
        while (counter < SAMPLE_SIZE) {
            sql.execute "INSERT INTO data_instance (tenantid, id, longvalue, clobvalue, discriminant, classname) VALUES (1, ?, null, '" + xmlDate + "', 'SXMLObjectDataInstanceImpl', 'java.util.Date')", 14 + counter
            sql.execute "INSERT INTO data_instance (tenantid, id, longvalue, clobvalue, discriminant, classname) VALUES (1, ? , null, '" + xmlNullDate + "', 'SXMLObjectDataInstanceImpl', 'java.util.Date')", 15 + counter
            sql.execute "INSERT INTO data_instance (tenantid, id, longvalue, clobvalue, discriminant, classname) VALUES (1, ?, null, null, 'SXMLObjectDataInstanceImpl', 'java.util.Date')", 16 + counter
            counter = counter + 3
        }
        def dateDataInstanceMigration = new MigrateDateDataInstancesFromWrongXMLObject(sql, dbVendor())

        //when
        dateDataInstanceMigration.migrate()

        //then
        def row = sql.firstRow("SELECT NAME, LONGVALUE, CLOBVALUE, DISCRIMINANT FROM data_instance where tenantid = 1 and ID = 14");
        def String name = row.getProperty("NAME")
        def Long dateAsLong = row.getProperty("LONGVALUE")
        def String dateAsClob = row.getProperty("CLOBVALUE")
        def String dataType = row.getProperty("DISCRIMINANT")
        assertThat(dateAsLong).isEqualTo(dateTime)
        assertThat(dateAsClob).isNull()
        assertThat(dataType).isEqualTo("SDateDataInstanceImpl")

        row = sql.firstRow("SELECT NAME, LONGVALUE, CLOBVALUE, DISCRIMINANT FROM data_instance where tenantid = 1 and ID = 15");
        name = row.getProperty("NAME")
        dateAsLong = row.getProperty("LONGVALUE")
        dateAsClob = row.getProperty("CLOBVALUE")
        dataType = row.getProperty("DISCRIMINANT")
        assertThat(dateAsLong).isNull()
        assertThat(dateAsClob).isNull()
        assertThat(dataType).isEqualTo("SDateDataInstanceImpl")

        row = sql.firstRow("SELECT NAME, LONGVALUE, CLOBVALUE, DISCRIMINANT FROM data_instance where tenantid = 1 and ID = 16");
        name = row.getProperty("NAME")
        dateAsLong = row.getProperty("LONGVALUE")
        dateAsClob = row.getProperty("CLOBVALUE")
        dataType = row.getProperty("DISCRIMINANT")
        assertThat(dateAsLong).isNull()
        assertThat(dateAsClob).isNull()
        assertThat(dataType).isEqualTo("SDateDataInstanceImpl")
    }

    void test_migrate_should_move_archived_data_instances_from_xmlobject_to_date_column() throws Exception {
        //given
        def Long dateTime = 1418660268855;
        def xmlDate = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><date>2014-12-15 16:17:48.855 UTC</date>";
        // equivalent to 1418660268855 in UTC and English locale (XStream defaults)
        def xmlNullDate = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><null></null>";

        def counter = 0
        while (counter < SAMPLE_SIZE) {
            sql.executeInsert "INSERT INTO arch_data_instance (tenantid, id, LONGVALUE, CLOBVALUE, DISCRIMINANT, ARCHIVEDATE, SOURCEOBJECTID, CLASSNAME) VALUES (1, ? , null, '${xmlDate}', 'SAXMLObjectDataInstanceImpl', 123456789, 14, 'java.util.Date')", 211 + counter
            sql.executeInsert "INSERT INTO arch_data_instance (tenantid, id, LONGVALUE, CLOBVALUE, DISCRIMINANT, ARCHIVEDATE, SOURCEOBJECTID, CLASSNAME) VALUES (1, ? , null, '" + xmlNullDate + "', 'SAXMLObjectDataInstanceImpl', 123456789, 14, 'java.util.Date')", 212 + counter
            sql.executeInsert "INSERT INTO arch_data_instance (tenantid, id, LONGVALUE, CLOBVALUE, DISCRIMINANT, ARCHIVEDATE, SOURCEOBJECTID, CLASSNAME) VALUES (1, ? , null, null, 'SAXMLObjectDataInstanceImpl', 123456789, 14, 'java.util.Date')", 213 + counter
            counter = counter + 3
        }
        def dateDataInstanceMigration = new MigrateDateDataInstancesFromWrongXMLObject(sql, dbVendor())

        //when
        dateDataInstanceMigration.migrate()

        //then
        def row = sql.firstRow("SELECT NAME, LONGVALUE, CLOBVALUE, DISCRIMINANT FROM arch_data_instance where tenantid = 1 and ID = 211");
        def String name = row.getProperty("NAME")
        def Long dateAsLong = row.getProperty("LONGVALUE")
        def String dateAsClob = row.getProperty("CLOBVALUE")
        def String dataType = row.getProperty("DISCRIMINANT")
        assertThat(dateAsLong).isEqualTo(dateTime)
        assertThat(dateAsClob).isNull()
        assertThat(dataType).isEqualTo("SADateDataInstanceImpl")

        row = sql.firstRow("SELECT NAME, LONGVALUE, CLOBVALUE, DISCRIMINANT FROM arch_data_instance where tenantid = 1 and ID = 212");
        name = row.getProperty("NAME")
        dateAsLong = row.getProperty("LONGVALUE")
        dateAsClob = row.getProperty("CLOBVALUE")
        dataType = row.getProperty("DISCRIMINANT")
        assertThat(dateAsLong).isNull()
        assertThat(dateAsClob).isNull()
        assertThat(dataType).isEqualTo("SADateDataInstanceImpl")

        row = sql.firstRow("SELECT NAME, LONGVALUE, CLOBVALUE, DISCRIMINANT FROM arch_data_instance where tenantid = 1 and ID = 213");
        name = row.getProperty("NAME")
        dateAsLong = row.getProperty("LONGVALUE")
        dateAsClob = row.getProperty("CLOBVALUE")
        dataType = row.getProperty("DISCRIMINANT")
        assertThat(dateAsLong).isNull()
        assertThat(dateAsClob).isNull()
        assertThat(dataType).isEqualTo("SADateDataInstanceImpl")
    }
}
