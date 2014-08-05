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

class ResetFailedGatewaysIT extends GroovyTestCase {
    final static String DBVENDOR
    final static CREATE_TABLE_6_3_2

    static {
        DBVENDOR = System.getProperty("db.vendor");
        CREATE_TABLE_6_3_2 = ResetFailedGatewaysIT.class.getClassLoader().getResource("sql/v6_3_2/${DBVENDOR}-createTables.sql");
    }

    JdbcDatabaseTester tester
    Sql sql


    Map trueValue = [
            "oracle"   : 1,
            "postgres" : true,
            "mysql"    : true,
            "sqlserver": true
    ]

    Map falseValue = [
            "oracle"   : 0,
            "postgres" : false,
            "mysql"    : false,
            "sqlserver": false
    ]

    def dataSet(data) {
        new FlatXmlDataSet(new StringReader(new StreamingMarkupBuilder().bind { dataset data }.toString()))
    }

    @Override
    void setUp() {
        String driverClass = System.getProperty("jdbc.driverClass")

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
        println("setUp: create tables")
        println("setUp: populating tables")
        tester.dataSet = dataSet {

            tenant id: 1
            tenant id: 2

            //gateway that will be reset to executing and will have left no archived
            flownode_instance tenantid: 1, id: 112, name: 'gate', parentContainerId: 130, kind: 'gate', stateId: 3, stateName: "failed", prev_state_id: 61, terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "1,5,12", token_ref_id: 10
            arch_flownode_instance tenantid: 1, id: 212, sourceObjectId: 112, stateId: 0, stateName: "executing", terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "1,5,12"

            //gateway that will be reset to completed and will have left one  archived version
            flownode_instance tenantid: 1, id: 113, name: 'gate', parentContainerId: 131, kind: 'gate', stateId: 3, stateName: "failed", prev_state_id: 2, terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "FINISH:5", token_ref_id: 11
            arch_flownode_instance tenantid: 1, id: 213, sourceObjectId: 113, stateId: 0, stateName: "executing", terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "1,5,12"
            arch_flownode_instance tenantid: 1, id: 214, sourceObjectId: 113, stateId: 2, stateName: "completed", terminal: trueValue.get(DBVENDOR), stable: trueValue.get(DBVENDOR), hitBys: "FINISH:5"

            flownode_instance tenantid: 1, id: 114, name: 'gate', parentContainerId: 132, kind: 'gate', stateId: 2, stateName: "completed", prev_state_id: 61, terminal: trueValue.get(DBVENDOR), stable: trueValue.get(DBVENDOR), hitBys: "FINISH:5", token_ref_id: 12
            flownode_instance tenantid: 1, id: 115, name: 'gate', parentContainerId: 133, kind: 'gate', stateId: 61, stateName: "executing", prev_state_id: 0, terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "1,5,12", token_ref_id: 13
            //gateway reset because hitBys is inconsistent with the state
            flownode_instance tenantid: 1, id: 116, name: 'gate', parentContainerId: 134, kind: 'gate', stateId: 3, stateName: "completed", prev_state_id: 61, terminal: trueValue.get(DBVENDOR), stable: trueValue.get(DBVENDOR), hitBys: "1,5,12", token_ref_id: 14

            //gateway was erroneously created because a transition hit the gateway when it was in completed with hitBys not like "FINISH%"
            //case all transition hit
            flownode_instance tenantid: 1, id: 117, name: 'gate1', parentContainerId: 125, kind: 'gate', stateId: 3, stateName: "failed", prev_state_id: 2, terminal: falseValue.get(DBVENDOR), stable: trueValue.get(DBVENDOR), hitBys: "1", token_ref_id: 2
            flownode_instance tenantid: 1, id: 118, name: 'gate1', parentContainerId: 125, kind: 'gate', stateId: 61, stateName: "executing", prev_state_id: 0, terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "2", token_ref_id: 2
            //create 2 token
            token tenantid: 1, id: 1, ref_id: 2, processInstanceId: 125
            token tenantid: 1, id: 2, ref_id: 2, processInstanceId: 125

            //case not all transition hit
            flownode_instance tenantid: 1, id: 119, name: 'gate2', parentContainerId: 126, kind: 'gate', stateId: 3, stateName: "failed", prev_state_id: 2, terminal: falseValue.get(DBVENDOR), stable: trueValue.get(DBVENDOR), hitBys: "1", token_ref_id: 3
            flownode_instance tenantid: 1, id: 120, name: 'gate2', parentContainerId: 126, kind: 'gate', stateId: 61, stateName: "executing", prev_state_id: 0, terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "2,3", token_ref_id: 3
            //create 3 token
            token tenantid: 1, id: 3, ref_id: 3, processInstanceId: 126
            token tenantid: 1, id: 4, ref_id: 3, processInstanceId: 126
            token tenantid: 1, id: 5, ref_id: 3, processInstanceId: 126
            token tenantid: 1, id: 6, ref_id: 3, processInstanceId: 126

            //on tenant 2
            flownode_instance tenantid: 2, id: 112, name: 'gate', parentContainerId: 137, kind: 'gate', stateId: 3, stateName: "failed", prev_state_id: 61, terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "1,5,12", token_ref_id: 1
            flownode_instance tenantid: 2, id: 113, name: 'gate', parentContainerId: 138, kind: 'gate', stateId: 3, stateName: "failed", prev_state_id: 2, terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "FINISH:5", token_ref_id: 2
            flownode_instance tenantid: 2, id: 114, name: 'gate', parentContainerId: 139, kind: 'gate', stateId: 2, stateName: "completed", prev_state_id: 61, terminal: trueValue.get(DBVENDOR), stable: trueValue.get(DBVENDOR), hitBys: "FINISH:5", token_ref_id: 3
            flownode_instance tenantid: 2, id: 115, name: 'gate', parentContainerId: 140, kind: 'gate', stateId: 61, stateName: "executing", prev_state_id: 0, terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "1,5,12", token_ref_id: 4

        }
        tester.onSetup();
    }

    @Override
    void tearDown() {
        tester.onTearDown()
        sql.execute("DROP TABLE arch_data_mapping")
        sql.execute("DROP TABLE arch_data_instance")
        sql.execute("DROP TABLE flownode_instance")
        sql.execute("DROP TABLE arch_flownode_instance")
        sql.execute("DROP TABLE tenant")
        sql.execute("DROP TABLE token")
    }

    void test_gateways_in_failed_are_reset() {
        def migration = new ResetFailedGateways();
        migration.migrate(DBVENDOR, sql);

        def updatedGateways = tester.connection.createDataSet("flownode_instance", "arch_flownode_instance");

        CustomAssertion.assertEquals dataSet {
            //gateway that will be reset to executing and will have left no archived
            flownode_instance tenantid: 1, id: 112, name: 'gate', parentContainerId: 130, kind: 'gate', stateId: 61, stateName: "executing", prev_state_id: 0, terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "1,5,12", token_ref_id: 10

            //gateway that will be reset to completed and will have left one  archived version
            flownode_instance tenantid: 1, id: 113, name: 'gate', parentContainerId: 131, kind: 'gate', stateId: 2, stateName: "completed", prev_state_id: 61, terminal: trueValue.get(DBVENDOR), stable: trueValue.get(DBVENDOR), hitBys: "FINISH:5", token_ref_id: 11
            arch_flownode_instance tenantid: 1, id: 213, sourceObjectId: 113, stateId: 0, stateName: "executing", terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "1,5,12"

            flownode_instance tenantid: 1, id: 114, name: 'gate', parentContainerId: 132, kind: 'gate', stateId: 2, stateName: "completed", prev_state_id: 61, terminal: trueValue.get(DBVENDOR), stable: trueValue.get(DBVENDOR), hitBys: "FINISH:5", token_ref_id: 12
            flownode_instance tenantid: 1, id: 115, name: 'gate', parentContainerId: 133, kind: 'gate', stateId: 61, stateName: "executing", prev_state_id: 0, terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "1,5,12", token_ref_id: 13
            flownode_instance tenantid: 1, id: 116, name: 'gate', parentContainerId: 134, kind: 'gate', stateId: 61, stateName: "executing", prev_state_id: 0, terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "1,5,12", token_ref_id: 14

            //gateway was erroneously created because a transition hit the gateway when it was in completed with hitBys not like "FINISH%"
            //case all transition hit
            flownode_instance tenantid: 1, id: 117, name: 'gate1', parentContainerId: 125, kind: 'gate', stateId: 2, stateName: "completed", prev_state_id: 61, terminal: trueValue.get(DBVENDOR), stable: trueValue.get(DBVENDOR), hitBys: "FINISH:2", token_ref_id: 2

            //case not all transition hit
            flownode_instance tenantid: 1, id: 119, name: 'gate2', parentContainerId: 126, kind: 'gate', stateId: 61, stateName: "executing", prev_state_id: 0, terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "1,2,3", token_ref_id: 3

            //on tenant 2
            flownode_instance tenantid: 2, id: 112, name: 'gate', parentContainerId: 137, kind: 'gate', stateId: 61, stateName: "executing", prev_state_id: 0, terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "1,5,12", token_ref_id: 1
            flownode_instance tenantid: 2, id: 113, name: 'gate', parentContainerId: 138, kind: 'gate', stateId: 2, stateName: "completed", prev_state_id: 61, terminal: trueValue.get(DBVENDOR), stable: trueValue.get(DBVENDOR), hitBys: "FINISH:5", token_ref_id: 2
            flownode_instance tenantid: 2, id: 114, name: 'gate', parentContainerId: 139, kind: 'gate', stateId: 2, stateName: "completed", prev_state_id: 61, terminal: trueValue.get(DBVENDOR), stable: trueValue.get(DBVENDOR), hitBys: "FINISH:5", token_ref_id: 3
            flownode_instance tenantid: 2, id: 115, name: 'gate', parentContainerId: 140, kind: 'gate', stateId: 61, stateName: "executing", prev_state_id: 0, terminal: falseValue.get(DBVENDOR), stable: falseValue.get(DBVENDOR), hitBys: "1,5,12", token_ref_id: 4
        }, updatedGateways
    }

}
