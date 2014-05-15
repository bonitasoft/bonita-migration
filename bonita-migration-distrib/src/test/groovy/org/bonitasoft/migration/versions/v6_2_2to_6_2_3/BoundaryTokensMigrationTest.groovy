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
package org.bonitasoft.migration.versions.v6_2_2to_6_2_3

import groovy.sql.Sql
import groovy.xml.StreamingMarkupBuilder

import org.bonitasoft.migration.CustomAssertion
import org.bonitasoft.migration.NamedColumnsAssertion
import org.dbunit.JdbcDatabaseTester
import org.dbunit.dataset.xml.FlatXmlDataSet


/**
 * @author Elias Ricken de Medeiros
 *
 */
class BoundaryTokensMigrationTest extends GroovyTestCase {

    final static DB_CONFIG
    final static DB_DRIVER = 'org.h2.Driver'
    final static BONITA_HOME_PATH = 'src/test/resources/bonita'
    final static CREATE_SEQUENCE_TABLE
    final static CREATE_FLOW_NODE_INSTANCE_TABLE
    final static CREATE_TOKEN_TABLE

    static{
        CREATE_SEQUENCE_TABLE = new File(BoundaryTokensMigrationTest.class.getResource("/sql/create-sequence-table.sql").getPath())
        CREATE_FLOW_NODE_INSTANCE_TABLE = new File(BoundaryTokensMigrationTest.class.getResource("/sql/create-flow-node-instance-table.sql").getPath())
        CREATE_TOKEN_TABLE = new File(BoundaryTokensMigrationTest.class.getResource("/sql/create-token-table.sql").getPath())
        DB_CONFIG = [
            'jdbc:h2:'+new File(BoundaryTokensMigrationTest.class.getResource("/sql/create-sequence-table.sql").getPath()).getAbsoluteFile().getParentFile().getParent()+'/db/sample',
            'sa',
            ''
        ];
    }

    JdbcDatabaseTester tester
    Sql sql


    @Override
    void setUp() {
        sql = Sql.newInstance(*DB_CONFIG, DB_DRIVER);
        sql.execute(CREATE_SEQUENCE_TABLE.text);
        sql.execute(CREATE_FLOW_NODE_INSTANCE_TABLE.text);
        sql.execute(CREATE_TOKEN_TABLE.text);
        tester = new JdbcDatabaseTester(DB_DRIVER, *DB_CONFIG)

        tester.dataSet = dataSet {
            sequence tenantid:1, id:10110, nextid:200
            sequence tenantid:2, id:10110, nextid:100

            //boundary
            flownode_instance tenantid:1, id:10, kind:"boundaryEvent", stateId:33, logicalGroup4:2, interrupting:true, activityInstanceId:9, token_ref_id:500, flownodeDefinitionId: 1234,
            rootContainerId:1, parentContainerId:2 , name:"go", prev_state_id:0, terminal:true, actorId:0, stateCategory:"NORMAL", logicalGroup1:12345,
            logicalGroup2:1, tokenCount:0
            flownode_instance tenantid:1, id:15, kind:"boundaryEvent", stateId:10, logicalGroup4:2, interrupting:true, activityInstanceId:14, token_ref_id:501, flownodeDefinitionId: 1234,
            rootContainerId:1, parentContainerId:2 , name:"go", prev_state_id:0, terminal:false, actorId:0, stateCategory:"NORMAL", logicalGroup1:12345,
            logicalGroup2:1, tokenCount:0
            flownode_instance tenantid:1, id:16, kind:"boundaryEvent", stateId:33, logicalGroup4:3, interrupting:true, activityInstanceId:14, token_ref_id:666, flownodeDefinitionId: 1234,
            rootContainerId:1, parentContainerId:2 , name:"go", prev_state_id:0, terminal:true, actorId:0, stateCategory:"NORMAL", logicalGroup1:12345,
            logicalGroup2:1, tokenCount:0
            flownode_instance tenantid:2, id:50, kind:"boundaryEvent", stateId:65, logicalGroup4:5, interrupting:false, activityInstanceId:49, token_ref_id:800, flownodeDefinitionId: 1234,
            rootContainerId:5, parentContainerId:5 , name:"go", prev_state_id:0, terminal:false, actorId:0, stateCategory:"NORMAL", logicalGroup1:12345,
            logicalGroup2:1, tokenCount:0
            flownode_instance tenantid:2, id:51, kind:"boundaryEvent", stateId:2, logicalGroup4:6, interrupting:false, activityInstanceId:49, token_ref_id:700, flownodeDefinitionId: 1234,
            rootContainerId:6, parentContainerId:6 , name:"go", prev_state_id:0, terminal:false, actorId:0, stateCategory:"NORMAL", logicalGroup1:12345,
            logicalGroup2:1, tokenCount:0

            //related activities
            flownode_instance tenantid:1, id:9, kind:"user", stateId:4, logicalGroup4:2, interrupting:false, token_ref_id:500, flownodeDefinitionId: 9821,
            rootContainerId:1, parentContainerId:2 , name:"step1", prev_state_id:0, terminal:false, actorId:0, stateCategory:"NORMAL", logicalGroup1:12345,
            logicalGroup2:1, tokenCount:0
            flownode_instance tenantid:1, id:14, kind:"user", stateId:4, logicalGroup4:2, interrupting:false, token_ref_id:501, flownodeDefinitionId: 9821,
            rootContainerId:1, parentContainerId:2 , name:"step1", prev_state_id:0, terminal:false, actorId:0, stateCategory:"NORMAL", logicalGroup1:12345,
            logicalGroup2:1, tokenCount:0
            flownode_instance tenantid:2, id:49, kind:"user", stateId:4, logicalGroup4:5, interrupting:false, token_ref_id:800, flownodeDefinitionId: 9821,
            rootContainerId:5, parentContainerId:5 , name:"step1", prev_state_id:0, terminal:false, actorId:0, stateCategory:"NORMAL", logicalGroup1:12345,
            logicalGroup2:5, tokenCount:0

            //tokens
            token tenantid:1, id:70, processInstanceId:2, ref_id:500, parent_ref_id:900
            token tenantid:1, id:71, processInstanceId:2, ref_id:501, parent_ref_id:900
            token tenantid:2, id:72, processInstanceId:5, ref_id:800, parent_ref_id:0


        }
        tester.onSetup();
    }

    @Override
    void tearDown() {
        tester.onTearDown()
        sql.execute("drop table sequence")
        sql.execute("drop table flownode_instance")
        sql.execute("drop table token")
    }

    void test_Migration_can_migrate_a_database() {
        BoundaryTokensMigration tokensMigration = new BoundaryTokensMigration();
        tokensMigration.migrate(sql);

        def updatedTokensAndSequences = tester.connection.createDataSet("token", "sequence");

        CustomAssertion.assertEquals dataSet {
            //sequenceid
            sequence tenantid:1, id:10110, nextid:202
            sequence tenantid:2, id:10110, nextid:101

            //tokens
            token tenantid:1, id:70, processInstanceId:2, ref_id:500, parent_ref_id:900
            token tenantid:1, id:71, processInstanceId:2, ref_id:501, parent_ref_id:900
            token tenantid:1, id:200, processInstanceId:2, ref_id:500, parent_ref_id:900
            token tenantid:1, id:201, processInstanceId:2, ref_id:501, parent_ref_id:900
            token tenantid:2, id:72, processInstanceId:5, ref_id:800, parent_ref_id:0
            token tenantid:2, id:100, processInstanceId:5, ref_id:50, parent_ref_id:"<skip>"
        }, updatedTokensAndSequences

        def updatedFlowNodeInstances = tester.connection.createDataSet("flownode_instance");
        NamedColumnsAssertion.assertEqualsForColumns dataSet{
            flownode_instance tenantid:1, id:9, stateId:4, token_ref_id:500
            flownode_instance tenantid:1, id:10, stateId:33, token_ref_id:500
            flownode_instance tenantid:1, id:14, stateId:4, token_ref_id:501
            flownode_instance tenantid:1, id:15, stateId:10, token_ref_id:501
            flownode_instance tenantid:1, id:16, stateId:33, token_ref_id:666
            flownode_instance tenantid:2, id:49, stateId:4, token_ref_id:800
            flownode_instance tenantid:2, id:50, stateId:65, token_ref_id:800
            flownode_instance tenantid:2, id:51, stateId:2, token_ref_id:51
        }, updatedFlowNodeInstances, [
            "tenantid",
            "id",
            "stateId",
            "token_ref_id"
        ]
    }



    def dataSet(data) {
        new FlatXmlDataSet(new StringReader(new StreamingMarkupBuilder().bind{dataset data}.toString()))
    }
}
