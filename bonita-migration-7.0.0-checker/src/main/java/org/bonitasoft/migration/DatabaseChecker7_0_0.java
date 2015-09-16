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

import static org.assertj.core.api.Assertions.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationCreator;
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
import org.junit.Test;
import org.junit.runner.JUnitCore;

public class DatabaseChecker7_0_0 extends SimpleDatabaseChecker7_0_0 {

    private static final String DISPLAY_NAME = "My Päge";
    private static final String CONTENT_NAME = "content.zip";
    private static final String PAGE_DESCRIPTION = "page description";

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker7_0_0.class.getName());
    }

    @Test
    public void should_deploy_a_business_data_model() throws Exception {
        final User user = getApiTestUtil().createUser("matti", "bpm");
        new BDMDataBaseChecker7_0_0().should_deploy_a_business_data_model(getApiTestUtil(), user);
        getIdentityApi().deleteUser(user.getId());
    }

    @Test
    public void should_be_able_to_create_an_application_after_migration() throws Exception {
        //when
        Application application = getApplicationAPI().createApplication(new ApplicationCreator("app", "my application", "1.0"));

        //then
        assertThat(application).isNotNull();
        assertThat(application.getLayoutId()).isNotNull();
        assertThat(application.getThemeId()).isNotNull();
    }

    @Test
    public void ensureFormMappingsAndStepContractWorks() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithFormMapping", "1.1");
        final String mainActor = "mainActor";
        builder.addActor(mainActor);
        //given
        final String taskName = "step1";
        final String inputName = "input";
        builder.addUserTask(taskName, mainActor).addContract().addInput(inputName, Type.TEXT, "should fail")
                .addConstraint("firstConstraint", "input != null", "mandatory", inputName);

        final User user = getIdentityApi().getUserByUserName("william.jobs");

        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setFormMappings(createDefaultProcessFormMapping(builder.getProcess()));

        // Add task here to have no form
        final String noFormAssociatedTask = "noFormAssociatedTask";
        builder.addUserTask(noFormAssociatedTask, mainActor);
        builder.addTransition(taskName, noFormAssociatedTask);

        final BusinessArchive businessArchive = businessArchiveBuilder.setProcessDefinition(builder.getProcess()).done();
        final ProcessDefinition pDef = getProcessAPI().deploy(businessArchive);
        getProcessAPI().addUserToActor(mainActor, pDef, user.getId());
        getProcessAPI().enableProcess(pDef.getId());

        final SearchOptions searchOptions = new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, pDef.getId())
                .filter(FormMappingSearchDescriptor.TASK, "step1")
                .done();
        final FormMapping formMapping = getProcessAPI().searchFormMappings(searchOptions).getResult().get(0);
        assertThat(formMapping.getTask()).isEqualTo(taskName);

        final SearchResult<FormMapping> searchResult = getProcessAPI().searchFormMappings(
                new SearchOptionsBuilder(0, 10).filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, pDef.getId()).done());
        assertThat(searchResult.getCount()).as("search results retrieved are not what they should be : %s", searchResult.getResult()).isEqualTo(4);
        assertThat(searchResult.getResult()).as("search results retrieved are not what they should be : %s", searchResult.getResult()).hasSize(4)
                .extracting("task", "processDefinitionId", "type", "target", "URL").contains(
                        tuple(taskName, pDef.getId(), FormMappingType.TASK, FormMappingTarget.URL, HTTP_SOME_URL_COM),
                        tuple(null, pDef.getId(), FormMappingType.PROCESS_START, FormMappingTarget.LEGACY, null),
                        tuple(null, pDef.getId(), FormMappingType.PROCESS_OVERVIEW, FormMappingTarget.LEGACY, null),
                        tuple(noFormAssociatedTask, pDef.getId(), FormMappingType.TASK, FormMappingTarget.UNDEFINED, null)
                );

        final ProcessInstance pi = getProcessAPI().startProcess(user.getId(), pDef.getId());
        final long task = getApiTestUtil().waitForUserTask(pi.getId(), taskName);
        try {
            getProcessAPI().assignUserTask(task, user.getId());
            getProcessAPI().executeUserTask(task, new HashMap<String, Serializable>());
            fail("a Constraint failed exception should have been raised");
        } catch (final ContractViolationException e) {
            System.out.println(e.getExplanations());
        }
        try {
            getProcessAPI().executeUserTask(task, Collections.singletonMap(inputName, (Serializable) ""));
            final long task2 = getApiTestUtil().waitForUserTask(pi.getId(), noFormAssociatedTask);
            getProcessAPI().assignUserTask(task2, user.getId());
            getProcessAPI().executeFlowNode(task2);
        } catch (final ContractViolationException e) {
            e.printStackTrace();
            fail("Cause: " + e.getMessage() + "\n" + e.getExplanations());
        }
        getApiTestUtil().waitForProcessToFinish(pi.getId());
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
    public void verify_migration_of_legacy_form_add_form_mapping() throws Exception {
        getApiTestUtil().loginOnDefaultTenantWithDefaultTechnicalUser();
        long processWithForms = getApiTestUtil().getProcessAPI().getProcessDefinitionId("ProcessWithLegacyForms", "5.0");

        final SearchResult<FormMapping> formMappingSearchResult = getApiTestUtil().getProcessAPI().searchFormMappings(
                new SearchOptionsBuilder(0, 10).filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, processWithForms).done());
        assertThat(formMappingSearchResult.getCount()).isEqualTo(3);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processWithForms);
        Thread.sleep(2000);
        final ActivityInstance activityInstance = getProcessAPI().getOpenActivityInstances(processInstance.getId(), 0, 1, ActivityInstanceCriterion.DEFAULT)
                .get(0);

        final String url = getPageAPI().resolvePageOrURL("processInstance/ProcessWithLegacyForms/5.0",
                getStringSerializableMap(processInstance.getId()), true).getUrl();
        final String url1 = getPageAPI().resolvePageOrURL("process/ProcessWithLegacyForms/5.0", getStringSerializableMap(processWithForms),
                true).getUrl();
        final String url2 = getPageAPI().resolvePageOrURL("taskInstance/ProcessWithLegacyForms/5.0/myUserTask",
                getStringSerializableMap(activityInstance.getId()), true).getUrl();
        assertThat(url).isEqualTo(
                "/bonita/portal/homepage?ui=form&locale=en&theme=" + processWithForms + "#mode=form&form=ProcessWithLegacyForms--5.0%24recap&instance="
                        + processInstance.getId() + "&recap=true");
        assertThat(url1).isEqualTo(
                "/bonita/portal/homepage?ui=form&locale=en&theme=" + processWithForms + "#mode=form&form=ProcessWithLegacyForms--5.0%24entry&process="
                        + processWithForms);
        assertThat(url2).isEqualTo(
                "/bonita/portal/homepage?ui=form&locale=en&theme=" + processWithForms + "#mode=form&form=ProcessWithLegacyForms--5.0--myUserTask%24entry&task="
                        + activityInstance.getId());
    }

    Map<String, Serializable> getStringSerializableMap(long id) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>();
        map.put("queryParameters", (Serializable) Collections.<String, Serializable> singletonMap("id", new String[] { String.valueOf(id) }));
        map.put("contextPath", "/bonita");
        map.put("locale", "en");
        return map;
    }

}
