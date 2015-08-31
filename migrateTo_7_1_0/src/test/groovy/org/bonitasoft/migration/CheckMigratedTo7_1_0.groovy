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

package org.bonitasoft.migration

import org.bonitasoft.engine.LocalServerTestsInitializer
import org.bonitasoft.engine.api.PlatformAPIAccessor
import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.form.FormMapping
import org.bonitasoft.engine.form.FormMappingSearchDescriptor
import org.bonitasoft.engine.form.FormMappingTarget
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.test.PlatformTestUtil
import org.bonitasoft.migration.filler.FillerUtils
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static org.assertj.core.api.Assertions.assertThat
import static org.bonitasoft.engine.form.FormMappingSearchDescriptor.PROCESS_DEFINITION_ID
import static org.bonitasoft.engine.form.FormMappingTarget.INTERNAL
import static org.bonitasoft.engine.form.FormMappingTarget.LEGACY
import static org.bonitasoft.engine.form.FormMappingType.TASK

/**
 * @author Baptiste Mesta
 */
class CheckMigratedTo7_1_0 {

    @BeforeClass
    public static void beforeClass() {
        FillerUtils.initializeEngineSystemProperties()
        startNode()
    }

    private static void startNode() {
        LocalServerTestsInitializer.instance.prepareEnvironment()
        def platformTestUtil = new PlatformTestUtil()
        def platform = platformTestUtil.loginOnPlatform()
        def platformApi = platformTestUtil.getPlatformAPI(platform)
        platformApi.startNode()
        platformTestUtil.logoutOnPlatform(platform)
    }

    @Test
    def void theTest() {
        TenantAPIAccessor.getLoginAPI().login("john", "bpm");
    }

    @Test
    def void migrateFormMapping() {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install");
        def page = TenantAPIAccessor.getCustomPageAPI(session).getPageByName("custompage_globalpage")

        def processAPI = TenantAPIAccessor.getProcessAPI(session)
        def processDefinitionId = processAPI.getProcessDefinitionId("processWithFormMapping", "1.0")

        /*formMappingModelBuilder.addProcessOverviewForm("theExternalUrl", FormMappingTarget.URL)
        formMappingModelBuilder.addTaskForm("unexisting_custom_page", FormMappingTarget.INTERNAL, "step1");
        formMappingModelBuilder.addTaskForm("custompage_globalpage", FormMappingTarget.INTERNAL, "step2");
        formMappingModelBuilder.addTaskForm(null, FormMappingTarget.LEGACY, "step3");
        formMappingModelBuilder.addTaskForm("theExternalUrlForStep4", FormMappingTarget.URL, "step4");
*/
        assertThat(processAPI.searchFormMappings(new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.TYPE, TASK).filter(FormMappingSearchDescriptor.TASK, "step1").filter(PROCESS_DEFINITION_ID, processDefinitionId)
                .done()).getResult().get(0))
                .isEqualToIgnoringGivenFields(new FormMapping(processDefinitionId: processDefinitionId, type: TASK, task: "step1", target: INTERNAL), "id","pageMappingKey","lastUpdateDate")
        assertThat(processAPI.searchFormMappings(new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.TYPE, TASK).filter(FormMappingSearchDescriptor.TASK, "step2").filter(PROCESS_DEFINITION_ID, processDefinitionId)
                .done()).getResult().get(0))
                .isEqualToIgnoringGivenFields(new FormMapping(processDefinitionId: processDefinitionId, type: TASK, task: "step2", target: INTERNAL, pageId: page.getId()),"id","pageMappingKey","lastUpdateDate")
        assertThat(processAPI.searchFormMappings(new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.TYPE, TASK).filter(FormMappingSearchDescriptor.TASK, "step3").filter(PROCESS_DEFINITION_ID, processDefinitionId)
                .done()).getResult().get(0))
                .isEqualToIgnoringGivenFields(new FormMapping(processDefinitionId: processDefinitionId, type: TASK, task: "step3", target: LEGACY),"id","pageMappingKey","lastUpdateDate")
        assertThat(processAPI.searchFormMappings(new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.TYPE, TASK).filter(FormMappingSearchDescriptor.TASK, "step4").filter(PROCESS_DEFINITION_ID, processDefinitionId)
                .done()).getResult().get(0))
                .isEqualToIgnoringGivenFields(new FormMapping(processDefinitionId: processDefinitionId, type: TASK, task: "step4", target: FormMappingTarget.URL),"id","pageMappingKey","lastUpdateDate")
        assertThat(processAPI.searchFormMappings(new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.TYPE, TASK).filter(FormMappingSearchDescriptor.TASK, "step5").filter(PROCESS_DEFINITION_ID, processDefinitionId)
                .done()).getResult().get(0))
                .isEqualToIgnoringGivenFields(new FormMapping(processDefinitionId: processDefinitionId, type: TASK, task: "step5", target: FormMappingTarget.NONE),"id","pageMappingKey","lastUpdateDate")


    }

    @AfterClass
    public static void afterClass() {
        new PlatformTestUtil().stopPlatformAndTenant(PlatformAPIAccessor.getPlatformAPI(new PlatformTestUtil().loginOnPlatform()), true)
    }
}