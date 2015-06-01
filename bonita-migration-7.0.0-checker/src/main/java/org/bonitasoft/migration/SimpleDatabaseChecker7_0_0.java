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
 */
package org.bonitasoft.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.tuple;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bonitasoft.engine.LocalServerTestsInitializer;
import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.engine.bpm.form.FormMappingDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
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
public class SimpleDatabaseChecker7_0_0 {

    public static final long PROCESS_DEFINITION_ID = 5846L;
    public static final String HTTP_SOME_URL_COM = "http://someUrl.com";
    private static final String DISPLAY_NAME = "My Päge";
    private static final String CONTENT_NAME = "content.zip";
    private static final String PAGE_DESCRIPTION = "page description";
    private static final PlatformTestUtil platformTestUtil = new PlatformTestUtil();
    private static final APITestUtil apiTestUtil = new APITestUtil();
    private ProcessAPI processAPI;
    private IdentityAPI identityApi;
    private CommandAPI commandApi;
    private APISession session;
    private PageAPI pageAPI;
    private ApplicationAPI applicationAPI;

    @BeforeClass
    public static void setup() throws Exception {
        LocalServerTestsInitializer.getInstance().prepareEnvironment();
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
    public static void teardown() throws Exception {
        apiTestUtil.logoutOnTenant();
        final PlatformSession pSession = apiTestUtil.loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(pSession);
        apiTestUtil.stopPlatformAndTenant(platformAPI, false);
        apiTestUtil.logoutOnPlatform(pSession);
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

    @Before
    public void before() throws BonitaException {
        getApiTestUtil().loginOnDefaultTenantWithDefaultTechnicalUser();
        session = apiTestUtil.getSession();
        processAPI = TenantAPIAccessor.getProcessAPI(session);
        identityApi = TenantAPIAccessor.getIdentityAPI(session);
        commandApi = TenantAPIAccessor.getCommandAPI(session);
        pageAPI = TenantAPIAccessor.getCustomPageAPI(session);
        applicationAPI = TenantAPIAccessor.getLivingApplicationAPI(session);
    }

    @Test
    public void ensureFormMappingsAndStepContractWorks() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithFormMapping", "1.1");
        builder.addActor("mainActor");
        //given
        final String taskName = "step1";
        final String inputName = "input";
        builder.addUserTask(taskName, "mainActor").addContract().addInput(inputName, Type.TEXT, "should fail")
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
        final FormMapping formMapping = getProcessAPI().searchFormMappings(searchOptions).getResult().get(0);
        assertThat(formMapping.getTask()).isEqualTo(taskName);

        final SearchResult<FormMapping> searchResult = getProcessAPI().searchFormMappings(
                new SearchOptionsBuilder(0, 10).filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, pDef.getId()).done());
        assertThat(searchResult.getCount()).as("search results retrieved are not what they should be : %s", searchResult.getResult()).isEqualTo(3);
        assertThat(searchResult.getResult()).as("search results retrieved are not what they should be : %s", searchResult.getResult()).hasSize(3)
                .extracting("task", "processDefinitionId", "type", "target", "URL").contains(
                        tuple(taskName, pDef.getId(), FormMappingType.TASK, FormMappingTarget.URL, HTTP_SOME_URL_COM),
                        tuple(null, pDef.getId(), FormMappingType.PROCESS_START, FormMappingTarget.LEGACY, null),
                        tuple(null, pDef.getId(), FormMappingType.PROCESS_OVERVIEW, FormMappingTarget.LEGACY, null)
                );

        final ProcessInstance pi = getProcessAPI().startProcess(user.getId(), pDef.getId());
        final long task = apiTestUtil.waitForUserTask(pi.getId(), taskName);
        try {
            getProcessAPI().assignUserTask(task, user.getId());
            getProcessAPI().executeUserTask(task, new HashMap<String, Serializable>());
            fail("a Constraint failed exception should have been raised");
        } catch (final ContractViolationException e) {
            System.out.println(e.getExplanations());
        }
        try {
            getProcessAPI().executeUserTask(task, Collections.singletonMap(inputName, (Serializable) ""));
        } catch (final ContractViolationException e) {
            e.printStackTrace();
            fail("Cause: " + e.getMessage() + "\n" + e.getExplanations());
        }
        apiTestUtil.waitForProcessToFinish(pi.getId());
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

    @Test
    public void can_complete_the_execution_of_previous_started_process_and_start_a_new_one() throws Exception {
        //given
        final User user = getIdentityApi().getUserByUserName("william.jobs");
        final long processDefinitionId = getProcessAPI().getProcessDefinitionId(SimpleDatabaseFiller6_0_2.PROCESS_NAME,
                SimpleDatabaseFiller6_0_2.PROCESS_VERSION);

        //when
        final SearchResult<ProcessInstance> searchResult = getProcessInstancesOfDefinition(processDefinitionId);

        //then (there are two instance, one created before migration and one created after migration)
        assertThat(searchResult.getCount()).isEqualTo(1);
        ProcessInstance processInstance = searchResult.getResult().get(0);

        //check the process started before migration is ok
        long taskInstance = getProcessAPI().getOpenActivityInstances(processInstance.getId(), 0, 1, ActivityInstanceCriterion.DEFAULT).get(0).getId();
        getProcessAPI().assignUserTask(taskInstance, user.getId());
        getProcessAPI().executeFlowNode(taskInstance);

        //then
        apiTestUtil.waitForProcessToFinish(processInstance.getId());

        //check we can still start this process after migration
        processInstance = getProcessAPI().startProcess(processDefinitionId);

        //when
        taskInstance = apiTestUtil.waitForUserTask(processInstance.getId(), SimpleDatabaseFiller6_0_2.USER_TASK_NAME);
        getProcessAPI().assignUserTask(taskInstance, user.getId());
        getProcessAPI().executeFlowNode(taskInstance);

        //then
        apiTestUtil.waitForProcessToFinish(processInstance.getId());
    }

    protected SearchResult<ProcessInstance> getProcessInstancesOfDefinition(final long processDefinitionId) throws SearchException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 100);
        builder.filter(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinitionId);
        return getProcessAPI().searchProcessInstances(builder.done());
    }

    public ProcessAPI getProcessAPI() {
        return processAPI;
    }

    public IdentityAPI getIdentityApi() {
        return identityApi;
    }

    public CommandAPI getCommandApi() {
        return commandApi;
    }

    public APISession getSession() {
        return session;
    }

    public PageAPI getPageAPI() {
        return pageAPI;
    }

    public ApplicationAPI getApplicationAPI() {
        return applicationAPI;
    }

    protected byte[] createTestPageContent(final String pageName, final String displayName, final String description)
            throws Exception {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);
            zos.putNextEntry(new ZipEntry("Index.groovy"));
            zos.write("return \"\";".getBytes());

            zos.putNextEntry(new ZipEntry("page.properties"));
            zos.write(("name=" + pageName + "\n" + "displayName=" + displayName + "\n" + "description=" + description + "\n").getBytes());

            zos.closeEntry();
            return baos.toByteArray();
        } catch (final IOException e) {
            throw new BonitaException(e);
        }
    }
}
