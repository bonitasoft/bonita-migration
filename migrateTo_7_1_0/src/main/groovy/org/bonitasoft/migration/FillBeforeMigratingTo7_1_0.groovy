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
import org.bonitasoft.engine.LocalServerTestsInitializer
import org.bonitasoft.engine.api.PlatformAPIAccessor
import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder
import org.bonitasoft.engine.bpm.form.FormMappingModelBuilder
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
import org.bonitasoft.engine.exception.BonitaException
import org.bonitasoft.engine.form.FormMappingSearchDescriptor
import org.bonitasoft.engine.form.FormMappingTarget
import org.bonitasoft.engine.form.FormMappingType
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.test.PlatformTestUtil
import org.bonitasoft.migration.filler.FillAction
import org.bonitasoft.migration.filler.FillerInitializer
import org.bonitasoft.migration.filler.FillerShutdown
import org.bonitasoft.migration.filler.FillerUtils

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
/**
 * @author Baptiste Mesta
 */
class FillBeforeMigratingTo7_1_0 {

    @FillerInitializer
    public void init() {
        FillerUtils.initializeEngineSystemProperties()
        LocalServerTestsInitializer.beforeAll();
    }


    @FillAction
    public void fillSomething() {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install");
        def identityAPI = TenantAPIAccessor.getIdentityAPI(session)
        identityAPI.createUser("john", "bpm")
        TenantAPIAccessor.getLoginAPI().logout(session)
    }




    @FillAction
    public void migrateFormMapping() {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install");
        TenantAPIAccessor.getCustomPageAPI(session).createPage("globalPage.zip", createTestPageContent("custompage_globalpage", "Global page", "a global page"));

        def builder = new ProcessDefinitionBuilder().createNewInstance("processWithFormMapping","1.0")
        builder.addActor("john")
        builder.addUserTask("step1","john")
        builder.addUserTask("step2","john")
        builder.addUserTask("step3","john")
        builder.addUserTask("step4","john")
        builder.addUserTask("step5","john")
        def barBuilder = new BusinessArchiveBuilder().createNewBusinessArchive()
        def formMappingModelBuilder = new FormMappingModelBuilder()
        formMappingModelBuilder.addProcessOverviewForm("theExternalUrl",FormMappingTarget.URL)
        formMappingModelBuilder.addTaskForm("unexisting_custom_page",FormMappingTarget.INTERNAL,"step1");
        formMappingModelBuilder.addTaskForm("custompage_globalpage",FormMappingTarget.INTERNAL,"step2");
        formMappingModelBuilder.addTaskForm(null,FormMappingTarget.LEGACY,"step3");
        formMappingModelBuilder.addTaskForm("theExternalUrlForStep4",FormMappingTarget.URL,"step4");
        barBuilder.setProcessDefinition(builder.done())
        barBuilder.setFormMappings(formMappingModelBuilder.build())

        def processAPI = TenantAPIAccessor.getProcessAPI(session)
        def processDefinition = processAPI.deploy(barBuilder.done())

        def formMapping = processAPI.searchFormMappings(new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.TYPE, FormMappingType.TASK).filter(FormMappingSearchDescriptor.TASK, "step2").filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, processDefinition.getId())
                .done()).getResult().get(0)
        if(!formMapping.getTarget().equals(FormMappingTarget.INTERNAL)){
            throw new IllegalStateException("wrong filler on the step1 need internal form mapping but was "+formMapping.getTarget())
        }

    }

    protected static byte[] createTestPageContent(String pageName, String displayName, String description) throws Exception {
        try {
            ByteArrayOutputStream e = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(e);
            zos.putNextEntry(new ZipEntry("Index.groovy"));
            zos.write("return \"\";".getBytes());
            zos.putNextEntry(new ZipEntry("page.properties"));
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("name=");
            stringBuilder.append(pageName);
            stringBuilder.append("\n");
            stringBuilder.append("displayName=");
            stringBuilder.append(displayName);
            stringBuilder.append("\n");
            stringBuilder.append("description=");
            stringBuilder.append(description);
            stringBuilder.append("\n");
            zos.write(stringBuilder.toString().getBytes());
            zos.closeEntry();
            return e.toByteArray();
        } catch (IOException var7) {
            throw new BonitaException(var7);
        }
    }

    @FillerShutdown
    public void shutdown() {
        new PlatformTestUtil().stopPlatformAndTenant(PlatformAPIAccessor.getPlatformAPI(new PlatformTestUtil().loginOnPlatform()), true)
    }

}
