/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.migration;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProcessConfigurationAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.engine.bpm.form.FormMappingDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.form.FormMappingSearchDescriptor;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.page.ContentType;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageAssert;
import org.bonitasoft.engine.page.PageCreator;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.ClientEventUtil;
import org.bonitasoft.engine.test.PlatformTestUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class SimpleDatabaseChecker7_0_0 extends SimpleDatabaseChecker6_4_0 {

    public static final long PROCESS_DEFINITION_ID = 5846L;

    private static final String DISPLAY_NAME = "My Päge";

    private static final String CONTENT_NAME = "content.zip";

    private static final String PAGE_DESCRIPTION = "page description";
    public static final String HTTP_SOME_URL_COM = "http://someUrl.com";

    private ProcessAPI processAPI;

    private IdentityAPI identityApi;

    private CommandAPI commandApi;

    private APISession session;

    protected volatile ProcessConfigurationAPI processConfigurationAPI;

    private static PlatformTestUtil platformTestUtil = new PlatformTestUtil();

    private static APITestUtil apiTestUtil = new APITestUtil();

    private PageAPI pageAPI;

    @BeforeClass
    public static void setup() throws BonitaException {
        setupSpringContext();
        final PlatformSession platformSession = platformTestUtil.loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        platformAPI.startNode();
        platformTestUtil.logoutOnPlatform(platformSession);

        apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalUser();
        // new version of class AddHandlerCommand.java in 6.3.3, that is useful for gatewayinstance_created event:
        ClientEventUtil.undeployCommand(apiTestUtil.getSession());
        ClientEventUtil.deployCommand(apiTestUtil.getSession());
    }

    @AfterClass
    public static void teardown() throws BonitaException {
        apiTestUtil.logoutOnTenant();
        final PlatformSession pSession = apiTestUtil.loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(pSession);
        apiTestUtil.stopPlatformAndTenant(platformAPI, false);
        apiTestUtil.logoutOnPlatform(pSession);
        closeSpringContext();
    }

    @Override
    @Before
    public void before() throws BonitaException {
        getApiTestUtil().loginOnDefaultTenantWithDefaultTechnicalUser();
        session = apiTestUtil.getSession();
        processAPI = TenantAPIAccessor.getProcessAPI(session);
        identityApi = TenantAPIAccessor.getIdentityAPI(session);
        commandApi = TenantAPIAccessor.getCommandAPI(session);
        pageAPI = TenantAPIAccessor.getCustomPageAPI(session);
    }

    @Test
    public void ensureFormMappingsAndStepContractWorks() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithFormMapping", "1.1");
        builder.addActor("mainActor");
        //given
        final String taskName = "step1";
        final String inputName = "input";
        builder.addUserTask(taskName, "mainActor").addContract().addSimpleInput(inputName, Type.TEXT, "should fail")
                .addConstraint("firstConstraint", "input != null", "mandatory", inputName);

        final User user = getIdentityApi().getUserByUserName("william.jobs");

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setFormMappings(createDefaultProcessFormMapping(builder.getProcess()))
                .setProcessDefinition(builder.getProcess())
                .done();
        final ProcessDefinition pDef = getProcessAPI().deploy(businessArchive);
        getProcessAPI().addUserToActor("mainActor", pDef, user.getId());
        getProcessAPI().enableProcess(pDef.getId());

        final SearchOptions searchOptions = new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, pDef.getId())
                .filter(FormMappingSearchDescriptor.TASK, "step1")
                .done();
        final FormMapping formMapping = getProcessConfigurationAPI().searchFormMappings(searchOptions).getResult().get(0);
        assertThat(formMapping.getTask()).isEqualTo(taskName);

        final SearchResult<FormMapping> searchResult = getProcessConfigurationAPI().searchFormMappings(new SearchOptionsBuilder(0, 10).done());
        assertThat(searchResult.getCount()).as("search results retrieved are not what they should be : %s", searchResult.getResult()).isEqualTo(3);
        assertThat(searchResult.getResult()).as("search results retrieved are not what they should be : %s", searchResult.getResult()).hasSize(3)
                .extracting("task", "processDefinitionId", "type", "target", "URL").contains(
                        tuple(taskName, pDef.getId(), FormMappingType.TASK, FormMappingTarget.URL, HTTP_SOME_URL_COM),
                        tuple(null, pDef.getId(), FormMappingType.PROCESS_START, FormMappingTarget.LEGACY, null),
                        tuple(null, pDef.getId(), FormMappingType.PROCESS_OVERVIEW, FormMappingTarget.LEGACY, null)
                );

        final ProcessInstance pi = getProcessAPI().startProcess(user.getId(), pDef.getId());
        final HumanTaskInstance task = waitForUserTask(taskName, pi.getId(), 2000);
        try {
            getProcessAPI().assignUserTask(task.getId(), user.getId());
            getProcessAPI().executeUserTask(task.getId(), new HashMap<String, Serializable>());
            fail("a Constraint failed exception should have been raised");
        } catch (final ContractViolationException e) {
            System.out.println(e.getExplanations());
        }
        try {
            getProcessAPI().executeUserTask(task.getId(), Collections.singletonMap(inputName, (Serializable) ""));
        } catch (final ContractViolationException e) {
            e.printStackTrace();
            fail("Cause: " + e.getMessage() + "\n" + e.getExplanations());
        }
        waitForProcessToFinish(pi.getId(), 3000);
    }

    @Test
    public void should_create_custom_page_with_type_and_process_definition() throws Exception {
        String pageName = "custompage_migration";
        final Page pageWithTenantScope = getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME).setContentType(ContentType.PAGE),
                createTestPageContent(pageName, DISPLAY_NAME, PAGE_DESCRIPTION));

        final Page pageWithProcessScope = getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME).setContentType(ContentType.FORM)
                        .setProcessDefinitionId(PROCESS_DEFINITION_ID),
                createTestPageContent(pageName, DISPLAY_NAME, PAGE_DESCRIPTION));

        // when
        final Page pageByName = getPageAPI().getPageByName(pageName);
        final Page pageByNameAndProcessDefinitionId = getPageAPI().getPageByNameAndProcessDefinitionId(pageName, PROCESS_DEFINITION_ID);

        // then
        assertThat(pageByNameAndProcessDefinitionId).isEqualToComparingFieldByField(pageWithProcessScope);
        assertThat(pageByName).isEqualToComparingFieldByField(pageWithTenantScope);
        PageAssert.assertThat(pageByNameAndProcessDefinitionId)
                .hasProcessDefinitionId(PROCESS_DEFINITION_ID)
                .hasContentType(ContentType.FORM);

        // clean up
        getPageAPI().deletePage(pageWithTenantScope.getId());
        getPageAPI().deletePage(pageWithProcessScope.getId());
    }

    public ProcessConfigurationAPI getProcessConfigurationAPI() throws Exception {
        if (processConfigurationAPI == null) {
            processConfigurationAPI = TenantAPIAccessor.getProcessConfigurationAPI(getSession());
        }
        return processConfigurationAPI;
    }

    @Override
    public ProcessAPI getProcessAPI() {
        return processAPI;
    }

    @Override
    public IdentityAPI getIdentityApi() {
        return identityApi;
    }

    @Override
    public CommandAPI getCommandApi() {
        return commandApi;
    }

    @Override
    public APISession getSession() {
        return session;
    }

    public static PlatformTestUtil getPlatformTestUtil() {
        return platformTestUtil;
    }

    public static APITestUtil getApiTestUtil() {
        return apiTestUtil;
    }

    public static FormMappingModel createDefaultProcessFormMapping(DesignProcessDefinition designProcessDefinition) {
        FormMappingModel formMappingModel = new FormMappingModel();
        formMappingModel.addFormMapping(FormMappingDefinitionBuilder.buildFormMapping(null, FormMappingType.PROCESS_START, FormMappingTarget.LEGACY)
                .build());
        formMappingModel.addFormMapping(FormMappingDefinitionBuilder.buildFormMapping(null, FormMappingType.PROCESS_OVERVIEW,
                FormMappingTarget.LEGACY).build());
        for (ActivityDefinition activityDefinition : designProcessDefinition.getFlowElementContainer().getActivities()) {
            if (activityDefinition instanceof UserTaskDefinition) {
                formMappingModel.addFormMapping(FormMappingDefinitionBuilder.buildFormMapping(HTTP_SOME_URL_COM, FormMappingType.TASK, FormMappingTarget.URL)
                        .withTaskname(activityDefinition.getName()).build());
            }
        }
        return formMappingModel;
    }

    public PageAPI getPageAPI() {
        return pageAPI;
    }

    protected byte[] createTestPageContent(final String pageName, final String displayName, final String description)
            throws Exception {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);
            zos.putNextEntry(new ZipEntry("Index.groovy"));
            zos.write("return \"\";".getBytes());

            zos.putNextEntry(new ZipEntry("page.properties"));
            final StringBuilder stringBuilder = new StringBuilder();
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
            return baos.toByteArray();
        } catch (final IOException e) {
            throw new BonitaException(e);
        }
    }
}
