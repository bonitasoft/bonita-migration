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

import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.bpm.process.ConfigurationState
import org.bonitasoft.engine.form.FormMapping
import org.bonitasoft.engine.form.FormMappingSearchDescriptor
import org.bonitasoft.engine.form.FormMappingTarget
import org.bonitasoft.engine.form.FormMappingType
import org.bonitasoft.engine.page.Page
import org.bonitasoft.engine.search.Order
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.search.SearchResult
import org.junit.Rule
import spock.lang.Specification
/**
 * @author Danila Mazour
 */
class CheckMigratedTo7_8_0 extends Specification {

    @Rule
    public After7_2_0Initializer initializer = new After7_2_0Initializer()

    def "should correctly migrate pages"() {
        given:
        def client = new APIClient()
        client.login("install", "install")

        when:
        Page page = client.customPageAPI.getPageByName("custompage_APIBirthdayBonita")

        then:
        page != null
        client.customPageAPI.getPageContent(page.id).size() > 0
        !page.hidden

        cleanup:
        client.logout()
    }

    def "should insert pages into migrated version with hidden field set to false"() {
        given:
        def client = new APIClient()
        client.login("install", "install")

        when:
        def content = CheckerUtils.createTestPageContent("custompage_APIBirthdayBonita2", "APIBirthdayBonita", "a custom page that should not be hidden")
        def page = client.customPageAPI.createPage("mypage.zip", content)

        then:
        page != null
        !client.customPageAPI.getPageByName("custompage_APIBirthdayBonita2").hidden

        cleanup:
        client.logout()
    }

    def "should have migrated v6 forms / pages"() {
        given:
        def client = new APIClient()
        client.login("install", "install")

        when:
        def processDefinitionId = client.processAPI.getProcessDefinitionId("ProcessWithV6CaseOverview", "7.7.4")
        def resource = client.processAPI.getProcessResources(processDefinitionId, 'resources/forms/forms.xml')
        SearchResult<FormMapping> formMappings = client.processAPI.searchFormMappings(new SearchOptionsBuilder(0, 5)
                .filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, processDefinitionId)
                .sort(FormMappingSearchDescriptor.TYPE, Order.ASC)
                .done())
        def processDeploymentInfo = client.processAPI.getProcessDeploymentInfo(processDefinitionId)

        then:
        resource.size() == 0

        formMappings.count == 3
        formMappings.result.get(0).target == FormMappingTarget.UNDEFINED
        formMappings.result.get(0).type == FormMappingType.PROCESS_START
        formMappings.result.get(1).target == FormMappingTarget.INTERNAL
        formMappings.result.get(1).type == FormMappingType.PROCESS_OVERVIEW
        formMappings.result.get(2).target == FormMappingTarget.UNDEFINED
        formMappings.result.get(2).type == FormMappingType.TASK

        processDeploymentInfo.configurationState == ConfigurationState.UNRESOLVED // it has been updated by migration

        cleanup:
        client.logout()
    }

}
