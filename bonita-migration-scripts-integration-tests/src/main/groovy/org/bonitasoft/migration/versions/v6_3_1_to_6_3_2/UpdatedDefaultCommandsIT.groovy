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
package org.bonitasoft.migration.versions.v6_3_1_to_6_3_2

import groovy.sql.Sql
import groovy.xml.StreamingMarkupBuilder

import org.bonitasoft.migration.CustomAssertion
import org.dbunit.JdbcDatabaseTester
import org.dbunit.dataset.xml.FlatXmlDataSet




/**
 * @author Elias Ricken de Medeiros
 *
 */
class UpdatedDefaultCommandsIT extends GroovyTestCase {
    final static String DBVENDOR
    final static CREATE_TABLE_6_3_1

    static{
        DBVENDOR = System.getProperty("db.vendor");
        CREATE_TABLE_6_3_1 = UpdatedDefaultCommandsIT.class.getClassLoader().getResource("sql/v6_3_1/${DBVENDOR}-createTables.sql");
    }

    JdbcDatabaseTester tester
    Sql sql

    def dataSet(data) {
        new FlatXmlDataSet(new StringReader(new StreamingMarkupBuilder().bind{ dataset data }.toString()))
    }

    Map trueValue =  [
        "oracle" : 1,
        "postgres": true,
        "mysql": true,
        "sqlserver" :true
    ]

    Map falseValue =  [
        "oracle" : 0,
        "postgres": false,
        "mysql": false,
        "sqlserver" :false
    ]


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

        def int i = 0
        CREATE_TABLE_6_3_1.text.split("@@").each({ stmt ->
            println "executing stmt ${i++} for ${DBVENDOR}"
            sql.execute(stmt)
        })
        println ("setUp: create tables")

        tester.dataSet = dataSet {
            //tenants
            tenant id:1
            tenant id:2

            //sequences
            sequence tenantid:1, id:90, nextid:200
            sequence tenantid:2, id:90, nextid:100

            //commands
            command tenantid:1, id:10, name:"command1", implementation:"org.bonitasoft.engine.Command1", description:"Command1 descr", system:trueValue.get(DBVENDOR)
            command tenantid:1, id:11, name:"command2", implementation:"org.bonitasoft.engine.Command2", description:"Command2 descr", system:falseValue.get(DBVENDOR)
            command tenantid:1, id:12, name:"command3", implementation:"org.bonitasoft.engine.Command3", description:"Command3 descr", system:trueValue.get(DBVENDOR)
            command tenantid:2, id:20, name:"command1", implementation:"org.bonitasoft.engine.Command1", description:"Command1 descr", system:trueValue.get(DBVENDOR)
            command tenantid:2, id:30, name:"command2", implementation:"org.bonitasoft.engine.Command1", description:"Command1 descr", system:falseValue.get(DBVENDOR)
        }
        tester.onSetup();
    }

    @Override
    void tearDown() {
        tester.onTearDown()
        sql.execute("DROP TABLE sequence")
        sql.execute("DROP TABLE command")
        sql.execute("DROP TABLE tenant")
    }

    void test_insert_new_system_command_should_add_a_new_line_per_tenant_and_update_sequence_table() {
        UpdatedDefaultCommands insertNewCommand = new UpdatedDefaultCommands();
        CommandDescriptor command1ToInsert = new CommandDescriptor(name:"newCommand1", implementation:"org.bonitasoft.engine.NewCommand1", description:"new command 1 description");
        CommandDescriptor command2ToInsert = new CommandDescriptor(name:"newCommand2", implementation:"org.bonitasoft.engine.NewCommand2", description:"new command 2 description");
        insertNewCommand.migrate(sql, [
            command1ToInsert,
            command2ToInsert
        ], []);

        def updatedCommandsAndSequences = tester.connection.createDataSet("sequence", "command");

        CustomAssertion.assertEquals dataSet {
            //sequences
            sequence tenantid:1, id:90, nextid:202
            sequence tenantid:2, id:90, nextid:102

            //commands
            command tenantid:1, id:10, name:"command1", implementation:"org.bonitasoft.engine.Command1", description:"Command1 descr", system:trueValue.get(DBVENDOR)
            command tenantid:1, id:11, name:"command2", implementation:"org.bonitasoft.engine.Command2", description:"Command2 descr", system:falseValue.get(DBVENDOR)
            command tenantid:1, id:12, name:"command3", implementation:"org.bonitasoft.engine.Command3", description:"Command3 descr", system:trueValue.get(DBVENDOR)
            command tenantid:1, id:200, name:"newCommand1", implementation:"org.bonitasoft.engine.NewCommand1", description:"new command 1 description", system:trueValue.get(DBVENDOR)
            command tenantid:1, id:201, name:"newCommand2", implementation:"org.bonitasoft.engine.NewCommand2", description:"new command 2 description", system:trueValue.get(DBVENDOR)
            command tenantid:2, id:20, name:"command1", implementation:"org.bonitasoft.engine.Command1", description:"Command1 descr", system:trueValue.get(DBVENDOR)
            command tenantid:2, id:30, name:"command2", implementation:"org.bonitasoft.engine.Command1", description:"Command1 descr", system:falseValue.get(DBVENDOR)
            command tenantid:2, id:100, name:"newCommand1", implementation:"org.bonitasoft.engine.NewCommand1", description:"new command 1 description", system:trueValue.get(DBVENDOR)
            command tenantid:2, id:101, name:"newCommand2", implementation:"org.bonitasoft.engine.NewCommand2", description:"new command 2 description", system:trueValue.get(DBVENDOR)
        }, updatedCommandsAndSequences
    }

    void test_insert_existing_system_command_should_have_no_effects() {
        UpdatedDefaultCommands insertNewCommand = new UpdatedDefaultCommands();
        //only the name is important
        CommandDescriptor commandToInsert = new CommandDescriptor(name:"command1", implementation:"org.bonitasoft.engine.NewCommand", description:"new command description");
        insertNewCommand.migrate(sql, [commandToInsert], []);

        def updatedCommandsAndSequences = tester.connection.createDataSet("sequence", "command");

        CustomAssertion.assertEquals dataSet {
            //sequences
            sequence tenantid:1, id:90, nextid:200
            sequence tenantid:2, id:90, nextid:100

            //commands
            command tenantid:1, id:10, name:"command1", implementation:"org.bonitasoft.engine.Command1", description:"Command1 descr", system:trueValue.get(DBVENDOR)
            command tenantid:1, id:11, name:"command2", implementation:"org.bonitasoft.engine.Command2", description:"Command2 descr", system:falseValue.get(DBVENDOR)
            command tenantid:1, id:12, name:"command3", implementation:"org.bonitasoft.engine.Command3", description:"Command3 descr", system:trueValue.get(DBVENDOR)
            command tenantid:2, id:20, name:"command1", implementation:"org.bonitasoft.engine.Command1", description:"Command1 descr", system:trueValue.get(DBVENDOR)
            command tenantid:2, id:30, name:"command2", implementation:"org.bonitasoft.engine.Command1", description:"Command1 descr", system:falseValue.get(DBVENDOR)
        }, updatedCommandsAndSequences
    }

    void test_delete_command_should_delete_command_for_all_tenants() {
        UpdatedDefaultCommands insertNewCommand = new UpdatedDefaultCommands();
        insertNewCommand.migrate(sql, [], ["command1"]);

        def updatedCommandsAndSequences = tester.connection.createDataSet("sequence", "command");

        CustomAssertion.assertEquals dataSet {
            //sequences
            sequence tenantid:1, id:90, nextid:200
            sequence tenantid:2, id:90, nextid:100

            //commands
            command tenantid:1, id:11, name:"command2", implementation:"org.bonitasoft.engine.Command2", description:"Command2 descr", system:falseValue.get(DBVENDOR)
            command tenantid:1, id:12, name:"command3", implementation:"org.bonitasoft.engine.Command3", description:"Command3 descr", system:trueValue.get(DBVENDOR)
            command tenantid:2, id:30, name:"command2", implementation:"org.bonitasoft.engine.Command1", description:"Command1 descr", system:falseValue.get(DBVENDOR)
        }, updatedCommandsAndSequences
    }
}
