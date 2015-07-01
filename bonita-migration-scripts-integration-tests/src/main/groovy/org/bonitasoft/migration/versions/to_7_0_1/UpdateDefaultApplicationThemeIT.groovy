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

package org.bonitasoft.migration.versions.to_7_0_1
import groovy.sql.Sql
import org.assertj.core.api.Assertions
import org.bonitasoft.migration.CustomAssertion
import org.dbunit.JdbcDatabaseTester

import static org.bonitasoft.migration.DBUnitHelper.*
/**
 * @author Elias Ricken de Medeiros
 */
class UpdateDefaultApplicationThemeIT extends GroovyTestCase {

    Sql sql
    JdbcDatabaseTester tester

    @Override
    void setUp() {
        sql = createSqlConnection();
        tester = createTester()

        createTables(sql, "7_0_0", "app-without-layout-theme");

        def temporaryContent = [97] as byte[];

        tester.dataSet = dataSet {
            tenant id: 1
            tenant id: 2

            page tenantId:1, id:40, name:"custompage_any", displayName:"Any", description:"just a page", installationDate: System.currentTimeMillis(), installedBy: -1,
                    provided:trueValue(), lastModificationDate: System.currentTimeMillis(), lastUpdatedBy: -1, contentName:"any.zip", content: temporaryContent,
                    contentType:"theme"
            page tenantId:1, id:41, name:"custompage_defaulttheme", displayName:"Default theme",
                    description:"Application theme based on Bootstrap \"Simplex\" theme. (see http://bootswatch.com/simplex/)", installationDate: System.currentTimeMillis(),
                    installedBy: -1, provided:trueValue(), lastModificationDate: System.currentTimeMillis(), lastUpdatedBy: -1, contentName:"bonita-default-theme.zip",
                    content: temporaryContent, contentType:"theme"

            page tenantId:2, id:60, name:"custompage_defaulttheme", displayName:"Default theme",
                    description:"Application theme based on Bootstrap \"Simplex\" theme. (see http://bootswatch.com/simplex/)", installationDate: System.currentTimeMillis(),
                    installedBy: -1, provided:trueValue(), lastModificationDate: System.currentTimeMillis(), lastUpdatedBy: -1, contentName:"bonita-default-theme.zip",
                    content: temporaryContent, contentType:"theme"
            page tenantId:2, id:61, name:"custompage_any", displayName:"Any", description:"just a page", installationDate: System.currentTimeMillis(), installedBy: -1,
                    provided:trueValue(), lastModificationDate: System.currentTimeMillis(), lastUpdatedBy: -1, contentName:"any.zip", content: temporaryContent,
                    contentType:"theme"
            page tenantId:2, id:62, name:"custompage_bootstrapdefaulttheme", displayName:"Bootstrap default theme",
                    description:"Application theme based on bootstrap \"Default\" theme. (see http://bootswatch.com/default/)", installationDate: System.currentTimeMillis(),
                    installedBy: -1, provided:trueValue(), lastModificationDate: System.currentTimeMillis(), lastUpdatedBy: -1, contentName:"bonita-bootstrap-default-theme.zip",
                    content: temporaryContent, contentType:"theme"

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

    void test_default_theme_should_be_renamed() {
        //when
        def updatedRows = new UpdateDefaultApplicationTheme(sql, dbVendor()).migrate()

        //then
        Assertions.assertThat(updatedRows).isEqualTo(3)
        def updatedData = tester.connection.createDataSet("page");

        CustomAssertion.assertEquals dataSet {
            //tenant 1
            page tenantId:1, id:40, name:"custompage_any", displayName:"Any", description:"just a page", installationDate: "<skip>", installedBy: -1,
                    provided:trueValue(), lastModificationDate: "<skip>", lastUpdatedBy: -1, contentName:"any.zip", content: "<skip>",
                    contentType:"theme", processDefinitionId:"<skip>"
            page tenantId:1, id:41, name:"custompage_bootstrapdefaulttheme", displayName:"Bootstrap default theme",
                    description:"Application theme based on bootstrap \"Default\" theme. (see http://bootswatch.com/default/)", installationDate: "<skip>",
                    installedBy: -1, provided:trueValue(), lastModificationDate: "<skip>", lastUpdatedBy: -1, contentName:"bonita-bootstrap-default-theme.zip",
                    content: "<skip>", contentType:"theme", processDefinitionId:"<skip>"

            //tenant 2
            page tenantId:2, id:60, name:"custompage_bootstrapdefaulttheme", displayName:"Bootstrap default theme",
                    description:"Application theme based on bootstrap \"Default\" theme. (see http://bootswatch.com/default/)", installationDate: "<skip>",
                    installedBy: -1, provided:trueValue(), lastModificationDate: "<skip>", lastUpdatedBy: -1, contentName:"bonita-bootstrap-default-theme.zip",
                    content: "<skip>", contentType:"theme", processDefinitionId:"<skip>"
            page tenantId:2, id:61, name:"custompage_any", displayName:"Any", description:"just a page", installationDate: "<skip>", installedBy: -1,
                    provided:trueValue(), lastModificationDate: "<skip>", lastUpdatedBy: -1, contentName:"any.zip", content: "<skip>",
                    contentType:"theme", processDefinitionId:"<skip>"
            page tenantId:2, id:62, name:"custompage_simplextheme", displayName:"Simplex theme",
                    description:"Application theme based on Bootstrap \"Simplex\" theme. (see http://bootswatch.com/simplex/)", installationDate: "<skip>",
                    installedBy: -1, provided:trueValue(), lastModificationDate: "<skip>", lastUpdatedBy: -1, contentName:"bonita-simplex-theme.zip",
                    content: "<skip>", contentType:"theme", processDefinitionId:"<skip>"



        }, updatedData

    }
}
