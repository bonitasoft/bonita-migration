/**
 * Copyright (C) 2015 BonitaSoft S.A.
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

package org.bonitasoft.migration.versions.to_7_0_0

import groovy.sql.Sql
import org.assertj.core.api.Assertions
import org.dbunit.JdbcDatabaseTester

import static org.bonitasoft.migration.DBUnitHelper.*

/**
 * @author Elias Ricken de Medeiros
 */
class ApplicationPageRetrieverIT extends GroovyTestCase {

    Sql sql
    JdbcDatabaseTester tester

    @Override
    void setUp() {
        sql = createSqlConnection();
        tester = createTester()

        createTables(sql, "6_5_2", "simplified-application");

        tester.dataSet = dataSet {
            tenant id: 1
            tenant id: 2
            tenant id: 3 // tenant without applications

            page tenantId:1, id:50, name: "custompage_first", displayName:"first example", provided:trueValue()
            page tenantId:1, id:60, name: "custompage_second", displayName:"second example", provided:trueValue()

            business_app tenantId: 1, id: 10, token: "app", version: "1.0", displayName: "Application"
            business_app tenantId: 1, id: 11, token: "content", version: "1.0", displayName: "Application content"
            business_app tenantId: 1, id: 12, token: "app-without-pages", version: "1.0", displayName: "Application without pages"

            business_app_page tenantId:1, id: 100, applicationId: 10, pageId:50, token:"CONTENT"
            business_app_page tenantId:1, id: 101, applicationId: 10, pageId:60, token:"valid"
            business_app_page tenantId:1, id: 102, applicationId: 11, pageId:50, token:"API"
            business_app_page tenantId:1, id: 103, applicationId: 11, pageId:60, token:"theme"

            page tenantId:2, id:10, name: "custompage_third", displayName:"third example", provided:trueValue()
            page tenantId:2, id:20, name: "custompage_fourth", displayName:"fourth example", provided:trueValue()

            business_app tenantId: 2, id: 10, token: "hr", version: "1.0", displayName: "Hr dashboard"

            business_app_page tenantId:2, id: 100, applicationId: 10, pageId:10, token:"content"
            business_app_page tenantId:2, id: 101, applicationId: 10, pageId:20, token:"page1"
            business_app_page tenantId:2, id: 102, applicationId: 11, pageId:10, token:"page2"
            business_app_page tenantId:2, id: 103, applicationId: 11, pageId:20, token:"page3"


        }
        tester.onSetup();

    }


    @Override
    void tearDown() {
        tester.onTearDown();

        def String[] strings = [
                "business_app_page",
                "business_app",
                "page",
                "tenant"
        ]
        dropTables(sql, strings)
    }


    public void testMigrate_should_retrieve_applications_with_forbidden_token() throws Exception {
        def applicationWithInvalidPages = new ApplicationPageRetriever(sql, dbVendor()).retrieveApplicationsWithInvalidPages()
        Assertions.assertThat(applicationWithInvalidPages).containsExactly(
                new TenantApplications(tenantId: 1, applications:
                        [new Application(tenantId: 1, id: 10, token: "app", version: "1.0", displayName: "Application", applicationPages: [
                                new ApplicationPage(id: 100, token: "CONTENT", page: new Page(name: "custompage_first", displayName: "first example"))
                        ]),
                         new Application(tenantId: 1, id: 11, token: "content", version: "1.0", displayName: "Application content", applicationPages: [
                                 new ApplicationPage(id: 102, token: "API", page: new Page(name: "custompage_first", displayName: "first example")),
                                 new ApplicationPage(id: 103, token: "theme", page: new Page(name: "custompage_second", displayName: "second example"))
                         ]
                         )]),
                new TenantApplications(tenantId: 2, applications: [
                        new Application(tenantId: 2, id: 10, token: "hr", version: "1.0", displayName: "Hr dashboard", applicationPages: [
                                new ApplicationPage(id: 100, token: "content", page: new Page(name: "custompage_third", displayName: "third example"))])
                ]))
    }
}
