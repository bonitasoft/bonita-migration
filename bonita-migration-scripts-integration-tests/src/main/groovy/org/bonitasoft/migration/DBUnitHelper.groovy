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
import org.dbunit.JdbcDatabaseTester
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.ReplacementDataSet
import org.dbunit.dataset.xml.FlatXmlDataSet
import org.dbunit.ext.oracle.OracleConnection

import java.sql.DriverManager

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

    static trueValue(){
        trueValueMap.get(dbVendor())
    }

    static falseValue(){
        falseValueMap.get(dbVendor())

    }

    static String dbVendor(){
        System.getProperty("db.vendor")
    }


    def static  dataSet(data) {
        new ReplacementDataSet(new FlatXmlDataSet(new StringReader(new StreamingMarkupBuilder().bind {
            dataset data
        }.toString())), ["[NULL]": null], null)
    }

    def static getCreateTables(String version, String feature){
        DBUnitHelper.class.getClassLoader().getResource("sql/v${version}/${dbVendor()}-${feature}.sql");
    }

    def static String[] createTables(sql, String feature) {
        println "Create tables of $feature"
        getCreateTables("6_4_0", feature).text.split("@@").each({ stmt ->
            sql.execute(stmt)
        })
    }

    def static JdbcDatabaseTester createTester() {
        new JdbcDatabaseTester(driverClass, url, user, password) {
            public IDatabaseConnection getConnection() {
                if (dbVendor() == "oracle") {
                    def conn = DriverManager.getConnection(getUrl(),getUser(), getPassword());
                    return new OracleConnection(conn, null);
                } else {
                    return super.getConnection();
                }
            }
        }
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
            sql.execute("DROP TABLE $it".toString())
        }
    }

}
