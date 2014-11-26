/**
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
 **/

package org.bonitasoft.migration.versions.v6_3_x_to_6_4_0

import groovy.sql.Sql
import org.bonitasoft.migration.CustomAssertion
import org.bonitasoft.migration.core.UpdateDefaultProfiles
import org.dbunit.JdbcDatabaseTester

import static org.bonitasoft.migration.DBUnitHelper.*

/**
 * @author Elias Ricken de Medeiros
 */
class UpdateDefaultProfilesIT extends GroovyTestCase {

    Sql sql
    JdbcDatabaseTester tester

    @Override
    void setUp() {
        sql = createSqlConnection();
        tester = createTester()

        createTables(sql, "profiles")

        def time = new Date().getTime()

        tester.dataSet = dataSet {
            tenant id: 1
            tenant id: 2

            //sequences
            sequence tenantid: 1, id: 9991, nextid: 601
            sequence tenantid: 2, id: 9991, nextid: 601

            profile tenantId: 1, id: 200, isDefault: trueValue(), name: "User", description: "MyDescription", creationDate: time, createdBy: 1, lastUpdateDate: time, lastUpdatedBy: 1
            profile tenantId: 1, id: 201, isDefault: trueValue(), name: "Administrator", description: "MyDescription", creationDate: time, createdBy: 1, lastUpdateDate: time, lastUpdatedBy: 1
            profile tenantId: 1, id: 202, isDefault: trueValue(), name: "Process manager", description: "MyDescription", creationDate: time, createdBy: 1, lastUpdateDate: time, lastUpdatedBy: 1

            profile tenantId: 2, id: 200, isDefault: trueValue(), name: "User", description: "MyDescription", creationDate: time, createdBy: 1, lastUpdateDate: time, lastUpdatedBy: 1
            profile tenantId: 2, id: 201, isDefault: trueValue(), name: "Administrator", description: "MyDescription", creationDate: time, createdBy: 1, lastUpdateDate: time, lastUpdatedBy: 1
            profile tenantId: 2, id: 202, isDefault: trueValue(), name: "Process manager", description: "MyDescription", creationDate: time, createdBy: 1, lastUpdateDate: time, lastUpdatedBy: 1

            profileentry tenantId: 1, id: 500, profileId: 201, name: "Configuration", description: "Manage apps", parentId: 0, index_: 4, type: "link", page: "anypage", custom: falseValue()

            profileentry tenantId: 2, id: 500, profileId: 201, name: "Configuration", description: "Manage apps", parentId: 0, index_: 4, type: "link", page: "anypage", custom: falseValue()
            profileentry tenantId: 2, id: 501, profileId: 201, name: "Apps", description: "Manage apps", parentId: 0, index_: 4, type: "link", page: "thememoredetailsadminext", custom: falseValue()


        }
        tester.onSetup();
    }


    @Override
    void tearDown() {
        tester.onTearDown();

        def String[] strings = ["profileentry",
                                "profile",
                                "sequence",
                                "tenant"
        ]
        dropTables(sql, strings)
    }


    void test_migration_documents_structure() {
        new UpdateDefaultProfiles(sql, dbVendor()).migrate();

        def updatedProfileEntries = tester.connection.createDataSet("profile", "profileentry", "sequence", "tenant");


        def iterator = updatedProfileEntries.iterator()
        while(iterator.next()) {

            def data = iterator.getTableMetaData()
            println "table:" + data.getTableName()
            def columns = data.getColumns()

            def table = iterator.getTable()
            def rowCount = table.getRowCount()
            for (int i = 0; i < rowCount; i++) {
                columns.each { row -> print row.columnName +": "+ table.getValue(i, row.columnName)+" " }
                print "\n"
            }
        }
        CustomAssertion.assertEquals dataSet {

            tenant id: 1
            tenant id: 2

            //sequences
            sequence tenantid: 1, id: 9991, nextid: 602
            sequence tenantid: 2, id: 9991, nextid: 601

            profile tenantId: 1, id: 200, isDefault: trueValue(), name: "User", description: "MyDescription", creationDate: "<skip>", createdBy: 1, lastUpdateDate: "<skip>", lastUpdatedBy: 1
            profile tenantId: 1, id: 201, isDefault: trueValue(), name: "Administrator", description: "MyDescription", creationDate: "<skip>", createdBy: 1, lastUpdateDate: "<skip>", lastUpdatedBy: 1
            profile tenantId: 1, id: 202, isDefault: trueValue(), name: "Process manager", description: "MyDescription", creationDate: "<skip>", createdBy: 1, lastUpdateDate: "<skip>", lastUpdatedBy: 1

            profile tenantId: 2, id: 200, isDefault: trueValue(), name: "User", description: "MyDescription", creationDate: "<skip>", createdBy: 1, lastUpdateDate: "<skip>", lastUpdatedBy: 1
            profile tenantId: 2, id: 201, isDefault: trueValue(), name: "Administrator", description: "MyDescription", creationDate: "<skip>", createdBy: 1, lastUpdateDate: "<skip>", lastUpdatedBy: 1
            profile tenantId: 2, id: 202, isDefault: trueValue(), name: "Process manager", description: "MyDescription", creationDate: "<skip>", createdBy: 1, lastUpdateDate: "<skip>", lastUpdatedBy: 1

            profileentry tenantId: 1, id: 500, profileId: 201, name: "Configuration", description: "Manage apps", parentId: 0, index_: 4, type: "link", page: "anypage", custom: falseValue()
            profileentry tenantId: 1, id: 601, profileId: 201, name: "Look & Feel", description: "LooknFeel", parentId: 500, index_: 2, type: "link", page: "thememoredetailsadminext", custom: falseValue()
            profileentry tenantId: 2, id: 500, profileId: 201, name: "Configuration", description: "Manage apps", parentId: 0, index_: 4, type: "link", page: "anypage", custom: falseValue()
            profileentry tenantId: 2, id: 501, profileId: 201, name: "Apps", description: "Manage apps", parentId: 0, index_: 4, type: "link", page: "thememoredetailsadminext", custom: falseValue()

        }, updatedProfileEntries
    }


}
