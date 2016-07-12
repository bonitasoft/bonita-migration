/*
 *
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.bonitasoft.migration

import groovy.sql.Sql
import groovy.xml.StreamingMarkupBuilder
import org.dbunit.DefaultOperationListener
import org.dbunit.IOperationListener
import org.dbunit.JdbcDatabaseTester
import org.dbunit.database.DatabaseConfig
import org.dbunit.database.DatabaseConnection
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.ReplacementDataSet
import org.dbunit.dataset.xml.FlatXmlDataSet
import org.dbunit.ext.mysql.MySqlConnection
import org.dbunit.ext.mysql.MySqlDataTypeFactory
import org.dbunit.ext.mysql.MySqlMetadataHandler
import org.dbunit.ext.oracle.OracleConnection

import java.sql.Connection
import java.sql.SQLException

/**
 * @author Baptiste Mesta
 */
class DBUnitHelper {

    static Map trueValueMap = [
            "oracle"   : 1,
            "postgres" : true,
            "mysql"    : true,
            "sqlserver": true
    ]
    static Map falseValueMap = [
            "oracle"   : 0,
            "postgres" : false,
            "mysql"    : false,
            "sqlserver": false
    ]

    static trueValue() {
        trueValueMap.get(dbVendor())
    }

    static falseValue() {
        falseValueMap.get(dbVendor())

    }

    static String dbVendor() {
        System.getProperty("db.vendor")
    }


    def static dataSet(data) {
        new ReplacementDataSet(new FlatXmlDataSet(new StringReader(new StreamingMarkupBuilder().bind {
            dataset data
        }.toString())), ["[NULL]": null], null)
    }

    def static getCreateTables(String version, String feature) {
        DBUnitHelper.class.getClassLoader().getResource("sql/v${version}/${dbVendor()}-${feature}.sql");
    }

    def static String[] createTables(sql, String feature) {
        createTables(sql, "6_4_0", feature);
    }

    def static String[] createTables(Sql sql, String version, String feature) {
        println "Create tables of sql/v${version}/$feature"
        sql.withTransaction {
            getCreateTables(version, feature).text.split("@@").each({ stmt ->
                sql.execute(stmt)
            })
        }
    }

    def static IDatabaseConnection createIDatabaseConnection(Connection connection) {
        switch (dbVendor()) {
            case "oracle":
                return new OracleConnection(connection, getUser())
                break
            case "mysql":
                MySqlConnection mySqlConnection = new MySqlConnection(connection, getUser())
                mySqlConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                        new MySqlDataTypeFactory());
                mySqlConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_METADATA_HANDLER,
                        new MySqlMetadataHandler());
                return mySqlConnection
                break
            default:
                return new DatabaseConnection(connection)
                break

        }
    }

    def static JdbcDatabaseTester createTester(Connection connection) {

        def tester = new JdbcDatabaseTester(driverClass, url, user, password) {

            @Override
            public IDatabaseConnection getConnection() {
                createIDatabaseConnection(connection)
            }

        }
        IOperationListener operationListener = new DefaultOperationListener() {

            @Override
            void connectionRetrieved(IDatabaseConnection iDatabaseConnection) {
                iDatabaseConnection.connection.setAutoCommit(false)
            }

            @Override
            void operationSetUpFinished(IDatabaseConnection iDatabaseConnection) {
                iDatabaseConnection.connection.commit()
            }
        }
        tester.setOperationListener(operationListener)
        tester
    }

    def static Sql createSqlConnection() {
        Sql.newInstance(url, user, password, driverClass)
    }

    def static String getPassword() {
        System.getProperty("jdbc.password")
    }

    def static String getUser() {
        System.getProperty("jdbc.user")
    }

    def static String getUrl() {
        System.getProperty("jdbc.url")
    }

    def static String getDriverClass() {
        System.getProperty("jdbc.driverClass")
    }

    def static dropTables(Sql sql, String[] tables) {
        tables.each {
            def statement = "DROP TABLE " + it
            println "executing statement [${statement}]"
            try {
                sql.withTransaction { sql.execute(statement as String) }
                println "table ${it} DROPPED"
            } catch (SQLException e) {
                println "failure ignored, table may not exists:" + e.getMessage()
            }
        }
    }

}
