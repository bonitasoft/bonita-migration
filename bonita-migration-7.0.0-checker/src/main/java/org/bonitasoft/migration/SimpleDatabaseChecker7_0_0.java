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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProcessConfigurationAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.identity.User;
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

    private ProcessAPI processAPI;

    private IdentityAPI identityApi;

    private CommandAPI commandApi;

    private APISession session;

    protected volatile ProcessConfigurationAPI processConfigurationAPI;

    private static PlatformTestUtil platformTestUtil = new PlatformTestUtil();

    private static APITestUtil apiTestUtil = new APITestUtil();

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
    }

    @Test
    public void checkStepContractWorks() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithContract", "1.0");
        builder.addActor("mainActor");

        //given
        final String taskName = "step1";
        final String inputName = "input";
        builder.addUserTask(taskName, "mainActor").addContract().addSimpleInput(inputName, Type.TEXT, "should fail")
        .addConstraint("firstConstraint", "input != null", "mandatory", inputName);

        //when
        final User user = getIdentityApi().getUserByUserName("william.jobs");
        final ProcessDefinition pDef = getProcessAPI().deploy(builder.done());
        getProcessAPI().addUserToActor("mainActor", pDef, user.getId());
        getProcessAPI().enableProcess(pDef.getId());
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
    public void checkFormMapping() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithContract", "1.1");
        builder.addActor("mainActor");
        //given
        final String taskName = "step1";
        final String inputName = "input";
        builder.addUserTask(taskName, "mainActor").addContract().addSimpleInput(inputName, Type.TEXT, "should fail")
        .addConstraint("firstConstraint", "input != null", "mandatory", inputName);

        final User user = getIdentityApi().getUserByUserName("william.jobs");
        final ProcessDefinition pDef = getProcessAPI().deploy(builder.done());
        getProcessAPI().addUserToActor("mainActor", pDef, user.getId());
        getProcessAPI().enableProcess(pDef.getId());

        final FormMapping formMapping = getProcessConfigurationAPI().getTaskForm(pDef.getId(), taskName);
        assertThat(formMapping.getTask()).isEqualTo(taskName);
        final String taskForm = "http://www.appli-form.org/step1";
        processConfigurationAPI.updateFormMapping(formMapping.getId(), taskForm, true);

        final SearchResult<FormMapping> searchResult = getProcessConfigurationAPI().searchFormMappings(new SearchOptionsBuilder(0, 10).done());
        assertThat(searchResult.getCount()).as("search results retrived are not what they should be : %s", searchResult.getResult()).isEqualTo(3);
        assertThat(searchResult.getResult()).as("search results retrived are not what they should be : %s", searchResult.getResult()).hasSize(3)
                .extracting("task", "processDefinitionId", "type", "form", "external").contains(
                tuple(taskName, pDef.getId(), FormMappingType.TASK, taskForm, true),
                tuple(null, pDef.getId(), FormMappingType.PROCESS_START, null, false),
                tuple(null, pDef.getId(), FormMappingType.PROCESS_OVERVIEW, null, false)
                );
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

}
