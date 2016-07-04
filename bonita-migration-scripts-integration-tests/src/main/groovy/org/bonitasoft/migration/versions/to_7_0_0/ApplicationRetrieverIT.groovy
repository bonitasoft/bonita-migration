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
class ApplicationRetrieverIT extends GroovyTestCase {

    Sql sql
    JdbcDatabaseTester tester

    @Override
    void setUp() {
        sql = createSqlConnection();
        tester = createTester(sql.connection)
        dropTestTables()
        createTables(sql, "6_5_2", "simplified-application");

        tester.dataSet = dataSet {
            tenant id: 1
            tenant id: 2
            tenant id: 3 //tenant without application

            business_app tenantId: 1, id: 10, token: "content-app", version: "1.0", displayName: "Content application"
            business_app tenantId: 1, id: 11, token: "app-content-11", version: "1.0", displayName: "Application content 11"
            business_app tenantId: 1, id: 12, token: "content", version: "1.0", displayName: "Content"
            business_app tenantId: 1, id: 13, token: "theme", version: "1.0", displayName: "Theme"
            business_app tenantId: 1, id: 14, token: "api", version: "1.0", displayName: "Api"

            business_app tenantId: 2, id: 10, token: "app-content", version: "1.0", displayName: "Application content"
            business_app tenantId: 2, id: 11, token: "CONTENT", version: "1.0", displayName: "CONTENT"
            business_app tenantId: 2, id: 22, token: "API", version: "1.0", displayName: "API"
            business_app tenantId: 2, id: 23, token: "THEME", version: "1.0", displayName: "THEME"

        }
        tester.onSetup();

    }


    @Override
    void tearDown() {
        dropTestTables()
        tester.onTearDown();
    }

    private void dropTestTables() {
        def String[] strings = [
                "business_app_page",
                "business_app",
                "page",
                "tenant"
        ]
        dropTables(sql, strings)
    }


    public void testMigrate_should_retrieve_applications_with_forbidden_token() throws Exception {
        def invalidApplications = new InvalidApplicationTokenRetriever(sql, dbVendor()).retrieveApplications()
        Assertions.assertThat(invalidApplications).containsExactly(
                new TenantApplications(tenantId: 1, applications:
                        [new Application(tenantId: 1, id: 12, token: "content", version: "1.0", displayName: "Content"),
                         new Application(tenantId: 1, id: 13, token: "theme", version: "1.0", displayName: "Theme"),
                         new Application(tenantId: 1, id: 14, token: "api", version: "1.0", displayName: "Api")]),
                new TenantApplications(tenantId: 2, applications: [
                        new Application(tenantId: 2, id: 11, token: "CONTENT", version: "1.0", displayName: "CONTENT"),
                        new Application(tenantId: 2, id: 22, token: "API", version: "1.0", displayName: "API"),
                        new Application(tenantId: 2, id: 23, token: "THEME", version: "1.0", displayName: "THEME")]))
    }
}
