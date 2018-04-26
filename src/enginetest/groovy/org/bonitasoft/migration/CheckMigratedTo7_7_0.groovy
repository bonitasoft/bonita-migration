/**
 * Copyright (C) 2017 Bonitasoft S.A.
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
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.junit.Rule
import spock.lang.Specification

/**
 * @author Laurent Leseigneur
 */
class CheckMigratedTo7_7_0 extends Specification {

    @Rule
    public After7_2_0Initializer initializer = new After7_2_0Initializer()

    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create().reuseExistingPlatform()

    def setupSpec() {
        CheckerUtils.initializeEngineSystemProperties()
    }

    def "verify we can login on migrated platform"() {
        given:
        def client = new APIClient()

        when:
        client.login("install", "install")

        then:
        client.session != null
    }

    def "should not have signal and message event trigger anymore"() {
        given:
        def client = new APIClient()
        client.login("install", "install")
        def processDefinition = client.processAPI.getProcessDefinitionId("processWithEventTriggers", "1.0")
        def processInstance = client.processAPI.searchProcessInstances(
                new SearchOptionsBuilder(0, 100).filter("processDefinitionId", processDefinition).done())
                .result.first()
        expect:
        client.processAPI.searchTimerEventTriggerInstances(processInstance.id,
                new SearchOptionsBuilder(0, 100).done()).count == 1
    }


    def "should have migrated contract data to the new serialization"() {
        given:
        def client = new APIClient()
        client.login("install","install")
        def processInstance = client.processAPI.searchArchivedProcessInstances(new SearchOptionsBuilder(0, 1).filter("name", "ProcessWithContract").done()).result[0]
        when:
        def contractDataValue = client.processAPI.getProcessInputValueAfterInitialization(processInstance.sourceObjectId, "myInput")
        then:
        contractDataValue == "theInputValue"
    }
}
