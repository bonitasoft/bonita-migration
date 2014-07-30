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

import groovy.sql.Sql
import groovy.xml.StreamingMarkupBuilder

import org.bonitasoft.migration.CustomAssertion
import org.dbunit.JdbcDatabaseTester
import org.dbunit.dataset.xml.FlatXmlDataSet

class MultiBusinessDataIT extends GroovyTestCase {
    final static String DBVENDOR
    final static CREATE_TABLE_6_4_0

    static{
        DBVENDOR = System.getProperty("db.vendor");
        CREATE_TABLE_6_4_0 = MultiBusinessDataIT.class.getClassLoader().getResource("sql/v6_4_0/${DBVENDOR}-createTables.sql");
    }

    JdbcDatabaseTester tester
    Sql sql

    def dataSet(data) {
        new FlatXmlDataSet(new StringReader(new StreamingMarkupBuilder().bind{ dataset data }.toString()))
    }

    @Override
    void setUp() {
        String driverClass =  System.getProperty("jdbc.driverClass")

        def config = [
            System.getProperty("jdbc.url"),
            System.getProperty("jdbc.user"),
            System.getProperty("jdbc.password")
        ]
        sql = Sql.newInstance(*config, driverClass);
        tester = new JdbcDatabaseTester(driverClass, *config)

        CREATE_TABLE_6_4_0.text.split("@@").each({ stmt ->
            println "executing stmt $stmt for ${DBVENDOR}"
            sql.execute(stmt)
        })
        println ("setUp: create table")
        println ("setUp: populating table")
        tester.dataSet = dataSet {
            ref_biz_data_inst tenantid:1, id:10, name:"employee", proc_inst_id:48
            ref_biz_data_inst tenantid:2, id:11, name:"address", proc_inst_id:4569
            ref_biz_data_inst tenantid:1, id:486, dataname:"a", proc_inst_id:48
            ref_biz_data_inst tenantid:2, id:48645, dataname:"a", proc_inst_id:4569
            ref_biz_data_inst tenantid:1, id:186, dataname:"a", proc_inst_id:78665
            ref_biz_data_inst tenantid:3, id:286, dataname:"a", proc_inst_id:13275
            ref_biz_data_inst tenantid:1, id:586, dataname:"a", proc_inst_id:1568
        }
        tester.onSetup();
    }

    @Override
    void tearDown() {
        tester.onTearDown()
        sql.execute("DROP TABLE ref_biz_data_inst")
    }

    void test_orphan_mappings_are_deleted() {
//        CleanArchivedDataMappingOrphans cleaner = new CleanArchivedDataMappingOrphans();
//        cleaner.migrate(sql);
//
//        def updatedArchivedData = tester.connection.createDataSet("arch_data_instance", "arch_data_mapping");

        CustomAssertion.assertEquals dataSet {
            ref_biz_data_inst tenantid:1, id:10, name:"employee", proc_inst_id:48, kind:"simple_ref"
            ref_biz_data_inst tenantid:2, id:11, name:"address", proc_inst_id:4569, kind:"simple_ref"
            ref_biz_data_inst tenantid:1, id:486, dataname:"a", proc_inst_id:48, kind:"simple_ref"
            ref_biz_data_inst tenantid:2, id:48645, dataname:"a", proc_inst_id:4569, kind:"simple_ref"
            ref_biz_data_inst tenantid:1, id:186, dataname:"a", proc_inst_id:78665, kind:"simple_ref"
            ref_biz_data_inst tenantid:3, id:286, dataname:"a", proc_inst_id:13275, kind:"simple_ref"
            ref_biz_data_inst tenantid:1, id:586, dataname:"a", proc_inst_id:1568, kind:"simple_ref"
        }, updatedArchivedData
    }
}
