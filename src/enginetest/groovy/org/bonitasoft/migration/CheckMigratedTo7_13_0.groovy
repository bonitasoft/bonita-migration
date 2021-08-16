/**
 * Copyright (C) 2021 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.migration

import groovy.sql.Sql
import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.business.application.Application
import org.bonitasoft.engine.business.application.ApplicationCreator
import org.bonitasoft.engine.business.application.ApplicationUpdater
import org.bonitasoft.engine.profile.Profile
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.junit.Rule
import spock.lang.Specification

import java.sql.SQLException

class CheckMigratedTo7_13_0 extends Specification {

    @Rule
    public After7_2_0Initializer initializer = new After7_2_0Initializer()

    def "should have icon available in application objects"() {
        given:
        def client = new APIClient()
        client.login("install", "install")

        when:
        def application = client.applicationAPI.createApplication(new ApplicationCreator("myapp1", "My application", "1.0").with {
            it.setIcon("myIcon.jpeg", "theContent".bytes)
            return it
        })
        then:
        def icon = client.applicationAPI.getIconOfApplication(application.id)
        icon.mimeType == "image/jpeg"
        icon.content == "theContent".bytes
    }


    def "should be able to update existing application"() {
        given:
        def client = new APIClient()
        client.login("install", "install")

        when:
        def oldApplication = client.applicationAPI.searchApplications(new SearchOptionsBuilder(0,100).filter("token", "myAppWithoutIcon").done()).result[0]
        def application = client.applicationAPI.updateApplication(oldApplication.id, new ApplicationUpdater().with {
            it.setIcon("myIcon.jpeg", "theContent".bytes)
            return it
        })
        then:
        def icon = client.applicationAPI.getIconOfApplication(application.id)
        icon.mimeType == "image/jpeg"
        icon.content == "theContent".bytes
    }

     def "should have new columns available in page objects"() {
        given:
        def client = new APIClient()
        client.login("install", "install")

        when:
        def page = client.customPageAPI.createPage("page-after-7.13.zip",
                TestUtil.createTestPageContent("custompage_PageAfter713", "PageAfter713", "A page created in 7.13.0"))
        def pageCreatedBefore713 = client.customPageAPI.getPageByName("custompage_PageBefore713")
        then:
        page.removable
        page.editable
        pageCreatedBefore713.removable
        pageCreatedBefore713.editable
    }

    def "profileEntry table should not exist anymore"() {
        given:
        def dburl = System.getProperty("db.url")
        def dbDriverClassName = System.getProperty("db.driverClass")
        def dbUser = System.getProperty("db.user")
        def dbPassword = System.getProperty("db.password")
        def sql = Sql.newInstance(dburl, dbUser, dbPassword, dbDriverClassName)

        when:
        sql.firstRow("SELECT * FROM profileentry")

        then:
        thrown(SQLException)
    }

    def "sequenceID for profileentry should not exist anymore"() {
        given:
        def dburl = System.getProperty("db.url")
        def dbDriverClassName = System.getProperty("db.driverClass")
        def dbUser = System.getProperty("db.user")
        def dbPassword = System.getProperty("db.password")
        def sql = Sql.newInstance(dburl, dbUser, dbPassword, dbDriverClassName)

        expect:
        sql.firstRow("SELECT count(*) FROM sequence WHERE id = ${9991}")[0] == 0
    }
}