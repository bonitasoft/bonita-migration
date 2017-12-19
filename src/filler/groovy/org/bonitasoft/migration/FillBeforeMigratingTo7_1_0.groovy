/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.migration

import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder
import org.bonitasoft.engine.bpm.form.FormMappingModelBuilder
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
import org.bonitasoft.engine.form.FormMappingSearchDescriptor
import org.bonitasoft.engine.form.FormMappingTarget
import org.bonitasoft.engine.form.FormMappingType
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.migration.filler.FillAction
import org.bonitasoft.migration.test.TestUtil
/**
 * @author Baptiste Mesta
 */
class FillBeforeMigratingTo7_1_0 {

    @FillAction
    public void migrateFormMapping() {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install");
        TenantAPIAccessor.getCustomPageAPI(session).createPage("globalPage.zip", TestUtil.createTestPageContent("custompage_globalpage", "Global page", "a global page"));

        def builder = new ProcessDefinitionBuilder().createNewInstance("processWithFormMapping", "1.0")
        builder.addActor("john")
        builder.addUserTask("step1", "john")
        builder.addUserTask("step2", "john")
        builder.addUserTask("step3", "john")
        builder.addUserTask("step4", "john")
        builder.addUserTask("step5", "john")
        def barBuilder = new BusinessArchiveBuilder().createNewBusinessArchive()
        def formMappingModelBuilder = new FormMappingModelBuilder()
        formMappingModelBuilder.addProcessOverviewForm("theExternalUrl", FormMappingTarget.URL)
        formMappingModelBuilder.addTaskForm("unexisting_custom_page", FormMappingTarget.INTERNAL, "step1");
        formMappingModelBuilder.addTaskForm("custompage_globalpage", FormMappingTarget.INTERNAL, "step2");
        formMappingModelBuilder.addTaskForm(null, FormMappingTarget.LEGACY, "step3");
        formMappingModelBuilder.addTaskForm("theExternalUrlForStep4", FormMappingTarget.URL, "step4");
        barBuilder.setProcessDefinition(builder.done())
        barBuilder.setFormMappings(formMappingModelBuilder.build())

        def processAPI = TenantAPIAccessor.getProcessAPI(session)
        def processDefinition = processAPI.deploy(barBuilder.done())

        def formMapping = processAPI.searchFormMappings(new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.TYPE, FormMappingType.TASK).filter(FormMappingSearchDescriptor.TASK, "step2").filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, processDefinition.getId())
                .done()).getResult().get(0)
        if (!formMapping.getTarget().equals(FormMappingTarget.INTERNAL)) {
            throw new IllegalStateException("wrong filler on the step1 need internal form mapping but was " + formMapping.getTarget())
        }

    }
}
