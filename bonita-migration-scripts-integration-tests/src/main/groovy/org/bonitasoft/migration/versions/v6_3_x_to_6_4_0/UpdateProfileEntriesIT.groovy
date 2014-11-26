package org.bonitasoft.migration.versions.v6_3_x_to_6_4_0

import groovy.sql.Sql
import org.bonitasoft.migration.CustomAssertion
import org.bonitasoft.migration.versions.v6_3_8_to_6_4_0.UpdateProfileEntries
import org.dbunit.JdbcDatabaseTester

import static org.bonitasoft.migration.DBUnitHelper.*

/**
 * @author Elias Ricken de Medeiros
 */
class UpdateProfileEntriesIT extends GroovyTestCase {

    Sql sql
    JdbcDatabaseTester tester

    @Override
    void setUp() {
        sql = createSqlConnection();
        tester = createTester()

        createTables(sql, "profiles")

        def time = new Date().getTime()

        tester.dataSet = dataSet {
            profile tenantId: 1, id: 200, isDefault: trueValue(), name: "User", description: "The user can view and perform tasks and can start a new case of an app.", creationDate: time, createdBy: 1, lastUpdateDate: time, lastUpdatedBy: 1
            profile tenantId: 1, id: 201, isDefault: trueValue(), name: "Administrator", description: "The administrator can install an app, manage the organization, and handle some errors (for example, by replaying a task).", creationDate: time, createdBy: 1, lastUpdateDate: time, lastUpdatedBy: 1
            profile tenantId: 1, id: 202, isDefault: trueValue(), name: "Process manager", description: "The Process manager can supervise designated apps, and manage cases and tasks of those apps.", creationDate: time, createdBy: 1, lastUpdateDate: time, lastUpdatedBy: 1

            profile tenantId: 2, id: 200, isDefault: trueValue(), name: "User", description: "The user can view and perform tasks and can start a new case of an app.", creationDate: time, createdBy: 1, lastUpdateDate: time, lastUpdatedBy: 1
            profile tenantId: 2, id: 201, isDefault: trueValue(), name: "Administrator", description: "The administrator can install an app, manage the organization, and handle some errors (for example, by replaying a task).", creationDate: time, createdBy: 1, lastUpdateDate: time, lastUpdatedBy: 1
            profile tenantId: 2, id: 202, isDefault: trueValue(), name: "Process manager", description: "The Process manager can supervise designated apps, and manage cases and tasks of those apps.", creationDate: time, createdBy: 1, lastUpdateDate: time, lastUpdatedBy: 1

            profileentry tenantId: 1, id: 500, profileId: 200, name: "Apps", description: "Manage apps", parentId: 0, index_: 4, type: "link", page: "anypage", custom: falseValue()
            profileentry tenantId: 1, id: 501, profileId: 201, name: "Apps management", description: "BPM", parentId: 0, index_: 0, type: "folder", page: "anypage", custom: falseValue()
            profileentry tenantId: 1, id: 502, profileId: 201, name: "Apps", description: "All processes", parentId: 501, index_: 4, type: "link", page: "anypage", custom: falseValue()
            profileentry tenantId: 1, id: 503, profileId: 202, name: "Apps", description: "My processes", parentId: 0, index_: 4, type: "link", page: "anypage", custom: falseValue()

            profileentry tenantId: 2, id: 500, profileId: 200, name: "Apps", description: "Manage apps", parentId: 0, index_: 4, type: "link", page: "anypage", custom: falseValue()
            profileentry tenantId: 2, id: 501, profileId: 201, name: "Apps management", description: "BPM", parentId: 0, index_: 0, type: "folder", page: "anypage", custom: falseValue()
            profileentry tenantId: 2, id: 502, profileId: 201, name: "Apps", description: "All processes", parentId: 501, index_: 4, type: "link", page: "anypage", custom: falseValue()
            profileentry tenantId: 2, id: 503, profileId: 202, name: "Apps", description: "My processes", parentId: 0, index_: 4, type: "link", page: "anypage", custom: falseValue()


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
        def feature = new File("build/dist/versions/6.3.8-6.4.0/Database/003_living_applications");
        new UpdateProfileEntries().migrate(feature, dbVendor(), sql);

        def updatedDocuments = tester.connection.createDataSet("profile", "profileentry");

        CustomAssertion.assertEquals dataSet {

            profile tenantId: 1, id: 200, isDefault: trueValue(), name: "User", description: "The user can view and perform tasks and can start a new case of a process.", creationDate: "<skip>", createdBy: 1, lastUpdateDate: "<skip>", lastUpdatedBy: 1
            profile tenantId: 1, id: 201, isDefault: trueValue(), name: "Administrator", description: "The administrator can install a process, manage the organization, and handle some errors (for example, by replaying a task).", creationDate: "<skip>", createdBy: 1, lastUpdateDate: "<skip>", lastUpdatedBy: 1
            profile tenantId: 1, id: 202, isDefault: trueValue(), name: "Process manager", description: "The Process manager can supervise designated processes, and manage cases and tasks of those processes.", creationDate: "<skip>", createdBy: 1, lastUpdateDate: "<skip>", lastUpdatedBy: 1

            profile tenantId: 2, id: 200, isDefault: trueValue(), name: "User", description: "The user can view and perform tasks and can start a new case of a process.", creationDate: "<skip>", createdBy: 1, lastUpdateDate: "<skip>", lastUpdatedBy: 1
            profile tenantId: 2, id: 201, isDefault: trueValue(), name: "Administrator", description: "The administrator can install a process, manage the organization, and handle some errors (for example, by replaying a task).", creationDate: "<skip>", createdBy: 1, lastUpdateDate: "<skip>", lastUpdatedBy: 1
            profile tenantId: 2, id: 202, isDefault: trueValue(), name: "Process manager", description: "The Process manager can supervise designated processes, and manage cases and tasks of those processes.", creationDate: "<skip>", createdBy: 1, lastUpdateDate: "<skip>", lastUpdatedBy: 1

            profileentry tenantId: 1, id: 500, profileId: 200, name: "Processes", description: "Manage processes", parentId: 0, index_: 4, type: "link", page: "anypage", custom: falseValue()
            profileentry tenantId: 1, id: 501, profileId: 201, name: "Process management", description: "BPM", parentId: 0, index_: 0, type: "folder", page: "anypage", custom: falseValue()
            profileentry tenantId: 1, id: 502, profileId: 201, name: "Processes", description: "All processes", parentId: 501, index_: 4, type: "link", page: "anypage", custom: falseValue()
            profileentry tenantId: 1, id: 503, profileId: 202, name: "Processes", description: "My processes", parentId: 0, index_: 4, type: "link", page: "anypage", custom: falseValue()

            profileentry tenantId: 2, id: 500, profileId: 200, name: "Processes", description: "Manage processes", parentId: 0, index_: 4, type: "link", page: "anypage", custom: falseValue()
            profileentry tenantId: 2, id: 501, profileId: 201, name: "Process management", description: "BPM", parentId: 0, index_: 0, type: "folder", page: "anypage", custom: falseValue()
            profileentry tenantId: 2, id: 502, profileId: 201, name: "Processes", description: "All processes", parentId: 501, index_: 4, type: "link", page: "anypage", custom: falseValue()
            profileentry tenantId: 2, id: 503, profileId: 202, name: "Processes", description: "My processes", parentId: 0, index_: 4, type: "link", page: "anypage", custom: falseValue()

        }, updatedDocuments
    }


}
