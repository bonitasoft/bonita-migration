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


import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.business.application.ApplicationCreator
import org.bonitasoft.engine.business.application.ApplicationUpdater
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.junit.Rule
import spock.lang.Specification

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

}
