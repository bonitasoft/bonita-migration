/**
 * Copyright (C) 2017-2018 Bonitasoft S.A.
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

import org.assertj.core.api.JUnitSoftAssertions
import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter
import org.bonitasoft.engine.bdm.model.BusinessObject
import org.bonitasoft.engine.bdm.model.BusinessObjectModel
import org.bonitasoft.engine.bdm.model.field.FieldType
import org.bonitasoft.engine.bdm.model.field.SimpleField
import org.bonitasoft.engine.exception.ContractDataNotFoundException
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.tenant.TenantResource
import org.bonitasoft.engine.tenant.TenantResourceState
import org.junit.Rule
import spock.lang.Specification

import static groovy.test.GroovyAssert.shouldFail
import static org.assertj.core.api.Assertions.assertThat
import static org.awaitility.Awaitility.await

/**
 * @author Laurent Leseigneur
 */
class CheckMigratedTo7_7_0 extends Specification {

    @Rule
    public After7_2_0Initializer initializer = new After7_2_0Initializer()

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions()

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

    def "should contract data archiving still work"() {
        def client = new APIClient()
        client.login("install", "install")
        def processInstance = client.processAPI.searchArchivedProcessInstances(new SearchOptionsBuilder(0, 1).filter("name", "ProcessWithContract").done()).result[0]

        shouldFail(ContractDataNotFoundException) {
            client.processAPI.getProcessInputValueAfterInitialization(processInstance.sourceObjectId, "myInput")
        }
        def processDefinitionId = client.processAPI.getProcessDefinitionId("ProcessWithContract", "1.0")
        def newProcessInstance = client.processAPI.startProcessWithInputs(processDefinitionId, [myInput: "theInputValue"])
        await().until({
            client.processAPI.getFinalArchivedProcessInstance(newProcessInstance.id).endDate != null
        })
        assertThat(client.processAPI.getProcessInputValueAfterInitialization(newProcessInstance.id, "myInput")).isEqualTo("theInputValue")
    }

    def "should have migrated BDM in Tenant Resource"() {
        given:
        def client = new APIClient()
        client.login("install", "install")
        when:
        TenantResource bdmResource = client.tenantAdministrationAPI.businessDataModelResource
        then:
        softly.assertThat(bdmResource.lastUpdateDate).describedAs('bdm tenant resource last update date').isNotNull()
        softly.assertThat(bdmResource.state).describedAs('bdm tenant resource state').isEqualTo(TenantResourceState.INSTALLED)
    }

    def "should be able to deploy a new BDM"() {
        given:
        def client = new APIClient()
        client.login("install", "install")
        client.tenantAdministrationAPI.pause()

        def businessObjectModel = createBusinessObjectModel()
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter()
        final byte[] zip = converter.zip(businessObjectModel)

        when:
        client.tenantAdministrationAPI.cleanAndUninstallBusinessDataModel()
        client.tenantAdministrationAPI.installBusinessDataModel(zip)
        client.tenantAdministrationAPI.resume()

        then:
        client.tenantAdministrationAPI.businessDataModelResource != null
    }

    private static BusinessObjectModel createBusinessObjectModel() {
        final SimpleField firstName = new SimpleField()
        firstName.setName("firstName")
        firstName.setType(FieldType.STRING)
        firstName.setLength(Integer.valueOf(50))
        final BusinessObject newEntity = new BusinessObject()
        newEntity.setQualifiedName("com.company.model.NewEntity")
        newEntity.addField(firstName)
        final BusinessObjectModel model = new BusinessObjectModel()
        model.addBusinessObject(newEntity)
        model
    }

}
