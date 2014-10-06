/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.migration.versions.v6_3_2_to_6_4_0

import static org.assertj.core.api.Assertions.assertThat
import groovy.sql.Sql
import groovy.xml.StreamingMarkupBuilder

import org.bonitasoft.migration.CustomAssertion
import org.bonitasoft.migration.versions.v6_3_x_to_6_4_0.CreateApplicationTables
import org.dbunit.JdbcDatabaseTester
import org.dbunit.dataset.xml.FlatXmlDataSet



/**
 * @author Elias Ricken de Medeiros
 *
 */
class CreateApplicationTablesIT  extends GroovyTestCase {
    final static String DBVENDOR
    final static CREATE_TABLES

    static{
        DBVENDOR = System.getProperty("db.vendor");
        CREATE_TABLES = CreateApplicationTablesIT.class.getClassLoader().getResource("sql/v6_4_0/${DBVENDOR}-createAppRelatedTables.sql");
    }


    static{
        DBVENDOR = System.getProperty("db.vendor");
    }

    def checkSql =  [
        "mysql" : { tableName -> """
            SELECT * FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = $tableName
        """ },
        "oracle" : { tableName -> """
            SELECT *
            FROM all_tables
            WHERE TABLE_NAME = $tableName
        """ },
        "postgres" : { tableName ->  """
                SELECT *
                FROM information_schema.tables
                WHERE table_name = $tableName
            """ },
        "sqlserver" : { tableName ->  """
                SELECT *
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_NAME = $tableName
            """ }
    ]

    def bussinessAppTable = [
        "mysql" : "business_app",
        "oracle" : "BUSINESS_APP",
        "postgres" : "business_app",
        "sqlserver" : "business_app"
    ];

    def busAppPageTable = [
        "mysql" : "business_app_page",
        "oracle" : "BUSINESS_APP_PAGE",
        "postgres" : "business_app_page",
        "sqlserver" : "business_app_page"];

    Sql sql
    JdbcDatabaseTester tester

    def dataSet(data) {
        new FlatXmlDataSet(new StringReader(new StreamingMarkupBuilder().bind{ dataset data }.toString()))
    }

    @Override
    void setUp() {
        String driverClass =  System.getProperty("jdbc.driverClass")

        def config = [System.getProperty("jdbc.url"), System.getProperty("jdbc.user"), System.getProperty("jdbc.password")]
        sql = Sql.newInstance(*config, driverClass);
        tester = new JdbcDatabaseTester(driverClass, *config)

        CREATE_TABLES.text.split("@@").each({ stmt ->
            println "executing stmt $stmt for ${DBVENDOR}"
            sql.execute(stmt)
        })

        println ("setUp: populating table")
        tester.dataSet = dataSet {
            tenant id:1
            tenant id:2
            tenant id:3
        }
        tester.onSetup();
    }

    @Override
    void tearDown() {
        tester.onTearDown();
        sql.execute("DROP TABLE business_app_page")
        sql.execute("DROP TABLE page")
        sql.execute("DROP TABLE business_app")
        sql.execute("DROP TABLE sequence")
        sql.execute("DROP TABLE tenant")
    }

    void test_createApplicationTables_should_create_application_tables_and_update_sequence_table() {
        def feature = new File("build/dist/versions/6.3.3-6.4.0/Database/003_living_applications")
        new CreateApplicationTables().migrate(feature, DBVENDOR, sql);

        def appTable = sql.firstRow(checkSql[DBVENDOR](bussinessAppTable[DBVENDOR]));
        def appPageTable = sql.firstRow(checkSql[DBVENDOR](busAppPageTable[DBVENDOR]));
        assertThat(appTable).isNotNull();
        assertThat(appPageTable).isNotNull();

        def updatedCommandsAndSequences = tester.connection.createDataSet("sequence");

        CustomAssertion.assertEquals dataSet {
            //sequences
            sequence tenantid:1, id:10200, nextid:1
            sequence tenantid:1, id:10201, nextid:1
            sequence tenantid:2, id:10200, nextid:1
            sequence tenantid:2, id:10201, nextid:1
            sequence tenantid:3, id:10200, nextid:1
            sequence tenantid:3, id:10201, nextid:1
        }, updatedCommandsAndSequences
    }
}
