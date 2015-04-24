package org.bonitasoft.migration.versions.v6_5_1_to_6_5_2
import groovy.sql.Sql
import org.bonitasoft.migration.CustomAssertion
import org.dbunit.JdbcDatabaseTester

import static org.bonitasoft.migration.DBUnitHelper.*
/**
 * @author Elias Ricken de Medeiros
 */
class UpdateEventSubProcessStableFlagIT extends GroovyTestCase {

    Sql sql
    JdbcDatabaseTester tester

    @Override
    void setUp() {
        sql = createSqlConnection();
        tester = createTester()

        createTables(sql, "6_5_1", "subproc");

        tester.dataSet = dataSet {
            tenant id: 1
            tenant id: 2

            flownode_instance tenantId: 1, id: 200, kind:"subProc", stateId: 31, terminal: falseValue(), stateName:"executing", stable: falseValue()
            flownode_instance tenantId: 1, id: 201, kind:"user", stateId: 31, terminal: falseValue(), stateName:"completing activity with boundary", stable: falseValue()
            flownode_instance tenantId: 1, id: 202, kind:"subProc", stateId: 31, terminal: falseValue(), stateName:"executing", stable: falseValue()

            flownode_instance tenantId: 2, id: 200, kind:"subProc", stateId: 31, terminal: falseValue(), stateName:"executing", stable: trueValue()
            flownode_instance tenantId: 2, id: 201, kind:"auto", stateId: 31, terminal: falseValue(), stateName:"completing activity with boundary", stable: falseValue()
            flownode_instance tenantId: 2, id: 210, kind:"subProc", stateId: 31, terminal: falseValue(), stateName:"executing", stable: falseValue()



        }
        tester.onSetup();

    }


    @Override
    void tearDown() {
        tester.onTearDown();

        def String[] strings = [
                "flownode_instance",
                "tenant"
        ]
        dropTables(sql, strings)
    }

    void test_migrate_should_add_update_stable_flag_to_true_for_executing_event_sub_processes() throws Exception {
        //when
        new UpdateEventSubProcessStableFlag(sql, dbVendor()).migrate()

        //then
        def updatedSubProcesses = tester.connection.createDataSet("flownode_instance");

        CustomAssertion.assertEquals dataSet {
            flownode_instance tenantId: 1, id: 200, kind:"subProc", stateId: 31, terminal: falseValue(), stateName:"executing", stable: trueValue()
            flownode_instance tenantId: 1, id: 201, kind:"user", stateId: 31, terminal: falseValue(), stateName:"completing activity with boundary", stable: falseValue()
            flownode_instance tenantId: 1, id: 202, kind:"subProc", stateId: 31, terminal: falseValue(), stateName:"executing", stable: trueValue()

            flownode_instance tenantId: 2, id: 200, kind:"subProc", stateId: 31, terminal: falseValue(), stateName:"executing", stable: trueValue()
            flownode_instance tenantId: 2, id: 201, kind:"auto", stateId: 31, terminal: falseValue(), stateName:"completing activity with boundary", stable: falseValue()
            flownode_instance tenantId: 2, id: 210, kind:"subProc", stateId: 31, terminal: falseValue(), stateName:"executing", stable: trueValue()

        }, updatedSubProcesses
    }

}
