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
import org.bonitasoft.migration.CustomAssertion
import org.dbunit.JdbcDatabaseTester

import static org.bonitasoft.migration.DBUnitHelper.*
/**
 * @author Elias Ricken de Medeiros
 */
class AddApplicationLayoutAndThemeIT extends GroovyTestCase {
    Sql sql
    JdbcDatabaseTester tester

    @Override
    void setUp() {
        sql = createSqlConnection();
        tester = createTester()

        createTables(sql, "7_0_0", "app-without-layout-theme");

        tester.dataSet = dataSet {
            tenant id: 1
            tenant id: 2

            sequence tenantid: 1, id: 10120, nextid: 50
            sequence tenantid: 2, id: 10120, nextid: 100

            business_app tenantId: 1, id: 10, token: "app10", version: "1.0", description: "The 10th application", iconPath: "/icon.png", creationDate: System.currentTimeMillis(), createdBy: 1, lastUpdateDate: System.currentTimeMillis(), updatedBy: 1, state: "ACTIVATED", displayName: "Application 10"
            business_app tenantId: 1, id: 11, token: "app11", version: "1.0", description: "The 11th application", iconPath: "/icon.png", creationDate: System.currentTimeMillis(), createdBy: 1, lastUpdateDate: System.currentTimeMillis(), updatedBy: 1, state: "ACTIVATED", displayName: "Application 11"

            business_app tenantId: 2, id: 11, token: "app11", version: "1.0", description: "The 11th application", iconPath: "/icon.png", creationDate: System.currentTimeMillis(), createdBy: 1, lastUpdateDate: System.currentTimeMillis(), updatedBy: 1, state: "ACTIVATED", displayName: "Application 11"

        }
        tester.onSetup();

    }


    @Override
    void tearDown() {
        tester.onTearDown();

        def String[] strings = [
                "business_app",
                "page",
                "sequence",
                "tenant"
        ]
        dropTables(sql, strings)
    }

    void testShouldInsertDefaultThemeAndLayout() {
        //when
        new AddApplicationLayoutAndTheme(sql, dbVendor()).migrate()

        //then
        def updatedData = tester.connection.createDataSet("sequence", "page", "business_app");

        CustomAssertion.assertEquals dataSet {
            sequence tenantid: 1, id: 10120, nextid: 52
            sequence tenantid: 2, id: 10120, nextid: 102

            //tenant 1
            //layout
            page tenantId:1, id:50, name:"custompage_layout", displayName:"Application layout page",
                    description: "This is a layout page dedicated to new born living applications. It is created and editable using the UI designer.",
                    installationDate:"<skip>", installedBy:-1 , provided: trueValue(), lastModificationDate:"<skip>", lastUpdatedBy: -1,
                    contentName:"bonita-layout-page.zip", content:"<skip>", contentType:"layout", processDefinitionId:"<skip>"
            //theme
            page tenantId:1, id:51, name:"custompage_theme", displayName:"Application theme",
                    description: "Application theme based on bootstrap \"Default\" theme. (see https://bootswatch.com/default/)",
                    installationDate:"<skip>", installedBy:-1 , provided: trueValue(), lastModificationDate:"<skip>", lastUpdatedBy: -1,
                    contentName:"bonita-theme-page.zip", content:"<skip>", contentType:"theme", processDefinitionId:"<skip>"

            //tenant 2
            //layout
            page tenantId:2, id:100, name:"custompage_layout", displayName:"Application layout page",
                    description: "This is a layout page dedicated to new born living applications. It is created and editable using the UI designer.",
                    installationDate:"<skip>", installedBy:-1 , provided: trueValue(), lastModificationDate:"<skip>", lastUpdatedBy: -1,
                    contentName:"bonita-layout-page.zip", content:"<skip>", contentType:"layout", processDefinitionId:"<skip>"
            //theme
            page tenantId:2, id:101, name:"custompage_theme", displayName:"Application theme",
                    description: "Application theme based on bootstrap \"Default\" theme. (see https://bootswatch.com/default/)",
                    installationDate:"<skip>", installedBy:-1 , provided: trueValue(), lastModificationDate:"<skip>", lastUpdatedBy: -1,
                    contentName:"bonita-theme-page.zip", content:"<skip>", contentType:"theme", processDefinitionId:"<skip>"

            //tenant 1
            business_app tenantId: 1, id: 10, token: "app10", version: "1.0", description: "The 10th application", iconPath: "/icon.png", creationDate: "<skip>",
                    createdBy: 1, lastUpdateDate: "<skip>", updatedBy: 1, state: "ACTIVATED", displayName: "Application 10", homepageid:"<skip>", profileId:"<skip>",
                    layoutId: 50, themeId:51
            business_app tenantId: 1, id: 11, token: "app11", version: "1.0", description: "The 11th application", iconPath: "/icon.png", creationDate: "<skip>",
                    createdBy: 1, lastUpdateDate: "<skip>", updatedBy: 1, state: "ACTIVATED", displayName: "Application 11", homepageid:"<skip>", profileId:"<skip>",
                    layoutId: 50, themeId:51

            //tenant2
            business_app tenantId: 2, id: 11, token: "app11", version: "1.0", description: "The 11th application", iconPath: "/icon.png", creationDate: "<skip>",
                    createdBy: 1, lastUpdateDate: "<skip>", updatedBy: 1, state: "ACTIVATED", displayName: "Application 11", homepageid:"<skip>", profileId:"<skip>",
                    layoutId: 100, themeId:101

        }, updatedData

    }
}
