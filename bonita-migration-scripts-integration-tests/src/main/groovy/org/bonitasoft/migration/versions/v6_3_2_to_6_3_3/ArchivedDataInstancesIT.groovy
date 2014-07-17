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
package org.bonitasoft.migration.versions.v6_3_2_to_6_3_3

import groovy.sql.Sql
import groovy.xml.StreamingMarkupBuilder

import org.bonitasoft.migration.CustomAssertion
import org.dbunit.JdbcDatabaseTester
import org.dbunit.dataset.xml.FlatXmlDataSet

class ArchivedDataInstancesIT extends GroovyTestCase {
    final static String DBVENDOR
    final static CREATE_TABLE_6_3_2

    static{
        DBVENDOR = System.getProperty("db.vendor");
        CREATE_TABLE_6_3_2 = ArchivedDataInstancesIT.class.getClassLoader().getResource("sql/v6_3_2/${DBVENDOR}-createTables.sql");
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

        CREATE_TABLE_6_3_2.text.split("@@").each({ stmt ->
            println "executing stmt $stmt for ${DBVENDOR}"
            sql.execute(stmt)
        })
        println ("setUp: create tables")
		println ("setUp: populating tables")
        tester.dataSet = dataSet {
        	// archive data instances
        	arch_data_instance tenantid:1, id:10, name:"employee", sourceobjectid:48
        	arch_data_instance tenantid:2, id:11, name:"address", sourceobjectid:4569

            // archive data visibilty mapping
            arch_data_mapping tenantid:1, id:486, dataname:"a", datainstanceid:48, sourceobjectid:786
            arch_data_mapping tenantid:2, id:48645, dataname:"a", datainstanceid:4569, sourceobjectid:78678
            arch_data_mapping tenantid:1, id:186, dataname:"a", datainstanceid:78665, sourceobjectid:7861
            arch_data_mapping tenantid:3, id:286, dataname:"a", datainstanceid:13275, sourceobjectid:7862
            arch_data_mapping tenantid:1, id:586, dataname:"a", datainstanceid:1568, sourceobjectid:786
            
            for (int j = 0; j < 500; j++) {
            	arch_data_mapping tenantid:1, id:1000 + j, dataname:"a", datainstanceid:156848 + j, sourceobjectid:687786
            	arch_data_mapping tenantid:2, id:1000 + j, dataname:"a", datainstanceid:1568736 + j, sourceobjectid:13786
            	arch_data_mapping tenantid:3, id:1000 + j, dataname:"a", datainstanceid:1568354 + j, sourceobjectid:468786
			}
        }
        tester.onSetup();
    }

    @Override
    void tearDown() {
        tester.onTearDown()
        sql.execute("DROP TABLE arch_data_mapping")
        sql.execute("DROP TABLE arch_data_instance")
    }

    void test_orphan_mappings_are_deleted() {
		CleanArchivedDataMappingOrphans cleaner = new CleanArchivedDataMappingOrphans();
		cleaner.migrate(sql);

        def updatedArchivedData = tester.connection.createDataSet("arch_data_instance", "arch_data_mapping");

        CustomAssertion.assertEquals dataSet {
        	// archive data instances
        	arch_data_instance tenantid:1, id:10, name:"employee", sourceobjectid:48
        	arch_data_instance tenantid:2, id:11, name:"address", sourceobjectid:4569
        
            // archive data visibilty mapping
            arch_data_mapping tenantid:1, id:486, dataname:"a", datainstanceid:48, sourceobjectid:786
            arch_data_mapping tenantid:2, id:48645, dataname:"a", datainstanceid:4569, sourceobjectid:78678
        }, updatedArchivedData
    }

}
