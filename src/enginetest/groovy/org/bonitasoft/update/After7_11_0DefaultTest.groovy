/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
package org.bonitasoft.update

import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.api.TenantAPIAccessor
import org.junit.Rule
import spock.lang.Specification
/**
 * @author Baptiste Mesta
 */
class After7_11_0DefaultTest extends Specification {

    @Rule
    public After7_2_0Initializer initializer = new After7_2_0Initializer()

    def "verify we can login with the tenant admin"() {
        given:
        def client = new APIClient()

        when:
        client.login("install", "install")

        then:
        client.session != null
    }

    def "verify we can login with user 'john'"() {
        expect:
        TenantAPIAccessor.getLoginAPI().login("john", "bpm")
    }

    def 'should be able to start a process with business data'() {
        given:
        def client = new APIClient()
        client.login("userToDeployAndStartProcess", "bpm")

        def processAPI = client.processAPI
        def processDefinitionId = processAPI.getProcessDefinitionId("ProcessWithBusinessData", "11.0")

        expect:
        processAPI.startProcess(processDefinitionId)
    }
}
