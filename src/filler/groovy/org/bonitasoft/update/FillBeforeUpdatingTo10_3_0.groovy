/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.update

import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.bpm.bar.BarResource
import org.bonitasoft.engine.bpm.bar.BusinessArchive
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException
import org.bonitasoft.engine.bpm.category.Category
import org.bonitasoft.engine.bpm.connector.ConnectorEvent
import org.bonitasoft.engine.bpm.contract.Type
import org.bonitasoft.engine.bpm.flownode.TimerType
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException
import org.bonitasoft.engine.bpm.process.ProcessDefinition
import org.bonitasoft.engine.bpm.process.ProcessInstance
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
import org.bonitasoft.engine.business.application.Application
import org.bonitasoft.engine.business.application.ApplicationCreator
import org.bonitasoft.engine.business.application.ApplicationMenuCreator
import org.bonitasoft.engine.business.application.ApplicationPage
import org.bonitasoft.engine.connector.AbstractConnector
import org.bonitasoft.engine.exception.BonitaException
import org.bonitasoft.engine.expression.Expression
import org.bonitasoft.engine.expression.ExpressionBuilder
import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.operation.LeftOperandBuilder
import org.bonitasoft.engine.operation.OperatorType
import org.bonitasoft.engine.profile.Profile
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.bonitasoft.update.filler.FillAction
import org.junit.Rule

import static java.util.Collections.singletonMap
import static org.bonitasoft.update.BuildTestUtil.*
import static org.bonitasoft.update.test.TestUtil.*
import static org.junit.Assert.assertEquals

class FillBeforeUpdatingTo10_3_0 {

    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create()

    @FillAction
    void 'execute complex process with connectors, data, multi-instance activity, etc'() {
        def client = new APIClient()
        client.login("install", "install")

        // Create a group
        def group = client.getIdentityAPI().createGroup(buildGroupAcme())
        // Create a role
        def role = client.getIdentityAPI().createRole(buildRoleMember())
        // Create a user with a manager
        def manager = client.getIdentityAPI().createUser(buildUserHelenKelly())
        def user = client.getIdentityAPI().createUser(buildUserWalterBates(manager.id))
        // Create a membership
        client.getIdentityAPI().addUserMembership(user.id, group.id, role.id)
        // Create a custom user info definition and value
        def definition = client.getIdentityAPI().createCustomUserInfoDefinition(
                new CustomUserInfoDefinitionCreator("Skype ID", "Skype ID of the user"))
        client.getIdentityAPI().setCustomUserInfoValue(definition.id, user.id, "live:walter.bates")

        client.logout()
        client.login("walter.bates", "bpm")

        final String valueOfInput1 = "valueOfInput1"
        final String defaultValue = "default"
        final String dataName = "myData1"
        final Expression dataDefaultValue = new ExpressionBuilder().createConstantStringExpression(defaultValue)
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1)
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnFinishOfAnAutomaticActivityWithDataAsOutput", "1.0")
        processDefinitionBuilder.addShortTextData(dataName, dataDefaultValue)
        String ACTOR_NAME = "actor"
        processDefinitionBuilder.addActor(ACTOR_NAME)
        def userTask = processDefinitionBuilder.addUserTask("step0", ACTOR_NAME)
        userTask.addContract().addInput("integerTaskContractData", Type.INTEGER, null)
        String CONNECTOR_WITH_OUTPUT_ID = "org.bonitasoft.connector.testConnectorWithOutput"
        String CONNECTOR_OUTPUT_NAME = "output1"
        String CONNECTOR_INPUT_NAME = "input1"
        processDefinitionBuilder
                .addAutomaticTask("step1")
                .addConnector("myConnector", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_FINISH)
                .addInput(CONNECTOR_INPUT_NAME, input1Expression)
                .addOutput(new LeftOperandBuilder().createNewInstance().setName(dataName).done(),
                OperatorType.ASSIGNMENT, "=", "",
                new ExpressionBuilder().createInputExpression(CONNECTOR_OUTPUT_NAME, String.class.getName()))
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME)
        processDefinitionBuilder.addTransition("step0", "step1")
        processDefinitionBuilder.addTransition("step1", "step2")

        processDefinitionBuilder.addContract().addInput("integerContractData", Type.INTEGER, null)

        ProcessAPI processAPI = client.getProcessAPI()
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, ACTOR_NAME, user,
                "TestConnectorWithOutput.impl", TestConnectorWithOutput.class, "TestConnectorWithOutput.jar", processAPI)

        // Test category and mapping:
        final Category category = processAPI.createCategory("my_process_category", "To better organize my processes")
        processAPI.addProcessDefinitionToCategory(category.id, processDefinition.getId())

        // Test supervisor:
        processAPI.createProcessSupervisorForUser(processDefinition.getId(), user.id)

        final ProcessInstance startProcess = processAPI.startProcessWithInputs(processDefinition.getId(), singletonMap("integerContractData", (Integer) 1))
        assertEquals(defaultValue, processAPI.getProcessDataInstance(dataName, startProcess.getId()).getValue())

        def step0ReadySearchOptions = getSearchOptionsForTask("step0")

        waitForUserTask("step0", processAPI)
        def result = processAPI.searchHumanTaskInstances(step0ReadySearchOptions).result
        if (result.empty) {
            throw new IllegalAccessException("Task 'step0' is not ready")
        }
        def taskInstance = result.get(0)
        processAPI.assignAndExecuteUserTask(user.id, taskInstance.id, singletonMap("integerTaskContractData", 22))

        waitForUserTask("step2", processAPI)
        assertEquals(valueOfInput1, processAPI.getProcessDataInstance(dataName, startProcess.getId()).getValue())

        // Test applications:
        final List<Profile> profiles = client.profileAPI.searchProfiles(new SearchOptionsBuilder(0, 1).done())
                .getResult()
        client.customPageAPI.createPage("layout.zip",
                createTestPageContent("custompage_layoutBonita", "LayoutBonita", "The default layout"))
        client.customPageAPI.createPage("theme.zip",
                createTestPageContent("custompage_themeBonita", "defaultTheme", "The default theme"))
        def homePage = client.customPageAPI.createPage("home.zip",
                createTestPageContent("custompage_homepage", "my-home-page", "My Home Page"))

        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dashboard", "1.0")
                .setProfileId(profiles.get(0).getId())
        final Application hr = client.applicationAPI.createApplication(hrCreator)
        final ApplicationPage applicationPage = client.applicationAPI.createApplicationPage(hr.id, homePage.id, "home_page_token")
        client.applicationAPI.createApplicationMenu(new ApplicationMenuCreator(hr.id, "Home Menu", applicationPage.id))
    }

    @FillAction
    def 'start process with timer or message'() throws Exception {
        def client = new APIClient()
        client.login("install", "install")
        def username = "walter.bates"
        def user = client.getIdentityAPI().getUserByUserName(username)
        client.logout()
        client.login(username, "bpm")

        final String ACTOR_NAME = "actor"
        final Expression timerExpression = new ExpressionBuilder().createConstantLongExpression(100)
        def processName = "process with start timer and start message"
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance(processName, "1.0")
        processDefinitionBuilder.addActor(ACTOR_NAME)
        processDefinitionBuilder.addStartEvent("startEventWithTimer")
                .addTimerEventTriggerDefinition(TimerType.DURATION, timerExpression)
                .addUserTask("step1WithTimer", ACTOR_NAME).addTransition("startEventWithTimer", "step1WithTimer")
        def startMessage = "message"
        def startEventWithMessage = "startEventWithMessage"
        processDefinitionBuilder.addStartEvent(startEventWithMessage).addMessageEventTrigger(startMessage)
                .addUserTask("step1WithMessage", ACTOR_NAME)
                .addTransition(startEventWithMessage, "step1WithMessage")
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(processDefinitionBuilder.done())

        deployAndEnableProcessWithActor(businessArchiveBuilder.done(), ACTOR_NAME, user, client.processAPI)
        sendMessage(startMessage, processName, startEventWithMessage, client.processAPI)

        waitForUserTask("step1WithMessage", client.processAPI)
        waitForUserTask("step1WithTimer", client.processAPI)
    }

    ProcessDefinition deployAndEnableProcessWithActorAndConnectorAndParameter(
            final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user, final String name,
            final Class<? extends AbstractConnector> clazz,
            final String jarName, ProcessAPI processAPI) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, actorName, user,
                Collections.singletonList(getContentAndBuildBarResource(name, clazz)),
                Collections.singletonList(generateJarAndBuildBarResource(clazz, jarName)), null, processAPI)
    }

    ProcessDefinition deployAndEnableProcessWithActorAndConnectorAndParameter(
            final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user, final List<BarResource> connectorImplementations,
            final List<BarResource> generateConnectorDependencies,
            final Map<String, String> parameters, ProcessAPI processAPI) throws BonitaException {
        try {
            final BusinessArchiveBuilder businessArchiveBuilder = buildBusinessArchiveWithConnectorAndUserFilter(processDefinitionBuilder,
                    connectorImplementations, generateConnectorDependencies, Collections.emptyList())
            if (parameters != null) {
                businessArchiveBuilder.setParameters(parameters)
            }
            return deployAndEnableProcessWithActor(businessArchiveBuilder.done(), actorName, user, processAPI)
        } catch (InvalidProcessDefinitionException | InvalidBusinessArchiveFormatException e) {
            throw new BonitaException(e)
        }
    }

    ProcessDefinition deployAndEnableProcessWithActor(BusinessArchive businessArchive, String actorName, User user, ProcessAPI processAPI) {
        def processDefinition = processAPI.deploy(businessArchive)
        processAPI.addUserToActor(actorName, processDefinition, user.id)
        processAPI.enableProcess(processDefinition.getId())
        return processDefinition
    }
}
