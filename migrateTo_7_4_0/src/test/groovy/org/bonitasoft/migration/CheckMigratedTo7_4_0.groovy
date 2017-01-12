/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

import groovy.sql.Sql
import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder
import org.bonitasoft.engine.bpm.contract.Type
import org.bonitasoft.engine.bpm.flownode.*
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
import org.bonitasoft.engine.expression.ExpressionBuilder
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.search.SearchResult
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.bonitasoft.migration.filler.FillerUtils
import org.junit.Rule
import spock.lang.Specification

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.groups.Tuple.tuple

/**
 * @author Emmanuel Duchastenier
 */
class CheckMigratedTo7_4_0 extends Specification {


    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create().reuseExistingPlatform()

    def setupSpec() {
        FillerUtils.initializeEngineSystemProperties()
    }

    def void "process should still work after xml migration"() {
        given:
        def client = new APIClient()
        client.login("userForMigratedProcess", "bpm")
        def processAPI = client.processAPI
        def id = processAPI.getProcessDefinitionId("MyProcess to be migrated", "to_7.4")

        def definition = processAPI.getDesignProcessDefinition(id)

        expect:
        assert definition.getName() == "MyProcess to be migrated"
        assert definition.getVersion() == "to_7.4"
        assert definition.description == "2-lines\ndescription"
        assert definition.displayDescription == "2-lines\ndisplay description"

        def startEvent = definition.flowElementContainer.startEvents.get(0)
        assert startEvent.description == "start event description"
        assert startEvent.displayDescription.content == "start event display description"
        assert startEvent.displayDescriptionAfterCompletion.content == "start event after completion display description"

        def endEvent = definition.flowElementContainer.endEvents.get(0)
        assert endEvent.description == "end event description"
        assert endEvent.displayDescription.content == "end event display description"
        assert endEvent.displayDescriptionAfterCompletion.content == "end event after completion display description"

        def dataDefinition = definition.getFlowElementContainer().getDataDefinitions().get(0)
        assert dataDefinition.name == "myData"
        assert dataDefinition.description == "my data description"

        def auto1 = definition.getFlowElementContainer().getActivity("auto1")
        def auto3 = definition.getFlowElementContainer().getActivity("taskWithNoDescription")

        def userTask1 = (UserTaskDefinition) definition.getFlowElementContainer().getActivity("user1")
        def userTask2 = (UserTaskDefinition) definition.getFlowElementContainer().getActivity("user2")

        def manualTask1 = (ManualTaskDefinition) definition.getFlowElementContainer().getActivity("manual1")
        def manualTask2 = (ManualTaskDefinition) definition.getFlowElementContainer().getActivity("manual2")

        assert userTask1 != null
        assert userTask1.getIncomingTransitions().size() == 1
        assert userTask1.getIncomingTransitions().get(0).getSource() == auto1.getId()
        assert userTask1.getContract().getInputs().get(0).description == "Serves description non-reg purposes"
        assert userTask1.getExpectedDuration() != null
        assert userTask1.getExpectedDuration().content == "3600000"
        assert userTask1.getExpectedDuration().returnType == Long.class.name

        assert auto1 != null
        assert auto1.getOutgoingTransitions().size() == 1
        assert auto1.getOutgoingTransitions().get(0).getTarget() == userTask1.getId()
        assert auto1.description == "autoTaskDesc"

        assert userTask2.getExpectedDuration() == null

        assert manualTask1.getExpectedDuration() != null
        assert manualTask1.getExpectedDuration().content == "3600000"
        assert manualTask1.getExpectedDuration().returnType == Long.class.name

        assert manualTask2.getExpectedDuration() == null

        assert auto3.description == null
        assert auto3.getConnectors().size() == 1
        assert auto3.getConnectors().get(0).getName() == "theConnector"
        assert auto3.getConnectors().get(0).getInputs().size() == 1
        assert auto3.getConnectors().get(0).getInputs().get("input1").getName() == "input1Value"
        assert auto3.getConnectors().get(0).getOutputs().size() == 1
        assert auto3.getOperations().size() == 1
        assert auto3.getOperations().get(0).getLeftOperand().name == "myData"

        assert definition.getFlowElementContainer().getTransitions().size() == 9
        assertThat(definition.getFlowElementContainer().getTransitions()).extracting("source", "target")
                .contains(tuple(auto1.getId(), userTask1.getId()))

        assert definition.getActorInitiator().getName() == "myActorInitiator"
        assert definition.getActorsList().size() == 2
        assert definition.getActorsList().get(0).getName() == "myActor"
        assert definition.getActorsList().get(1).getName() == "myActorInitiator"

        assert definition.getFlowElementContainer().getDataDefinition("xmlData") != null
        assert definition.getFlowElementContainer().getDataDefinition("xmlData").class.name.contains("XMLDataDefinition")
        assert definition.getFlowElementContainer().getDataDefinition("xmlData").getDefaultValueExpression().getContent() == "'<tag>'+isOk+'</tag>'"
        assert definition.getFlowElementContainer().getDataDefinition("xmlData").getDefaultValueExpression().getDependencies().size() == 1


        def auto4 = definition.getFlowElementContainer().getActivity("auto4")
        assert auto4.loopCharacteristics instanceof MultiInstanceLoopCharacteristics
        assert !(auto4.loopCharacteristics as MultiInstanceLoopCharacteristics).sequential
        assert (auto4.loopCharacteristics as MultiInstanceLoopCharacteristics).loopCardinality.content == "12"

        assert definition.contract.inputs.size() == 2
        assert definition.contract.inputs[0].multiple
        assert definition.contract.inputs[0].type == Type.BOOLEAN
        assert definition.contract.inputs[0].name == "isOk"
        assert definition.contract.inputs[0].description == "the is ok contract input"
        assert definition.contract.inputs[1].hasChildren()
        assert definition.contract.inputs[1].inputs[0].name == "name"
        assert definition.contract.inputs[1].inputs[1].name == "value"
        assert definition.contract.inputs[1].inputs[1].type == Type.INTEGER


        assert definition.getFlowElementContainer().getGateway("gate1").getDefaultTransition().target == definition.getFlowElementContainer().getActivity("auto3").id
        assert (definition.getFlowElementContainer().getActivity("call") as CallActivityDefinition).processStartContractInputs.containsKey("theInput")

        def humanTaskInstances = processAPI.getPendingHumanTaskInstances(client.session.userId, 0, 10, ActivityInstanceCriterion.NAME_ASC)
        def flownodeDefinitionIds = humanTaskInstances.collect() {
            [name: it.name, id: it.flownodeDefinitionId]
        }
        flownodeDefinitionIds == [
                [name: manualTask1.name, id: manualTask1.id],
                [name: manualTask2.name, id: manualTask2.id],
                [name: userTask1.name, id: userTask1.id],
                [name: userTask2.name, id: userTask2.id]
        ]

        cleanup:
        client.logout()

    }

    def "should not have the bpm event handling job anymore"() {
        given:
        def dburl = System.getProperty("db.url")
        def dbDriverClassName = System.getProperty("db.driverClass")
        def dbUser = System.getProperty("db.user")
        def dbPassword = System.getProperty("db.password")
        def sql = Sql.newInstance(dburl, dbUser, dbPassword, dbDriverClassName)
        expect:
        sql.firstRow("select count(*) from QRTZ_CRON_TRIGGERS where TRIGGER_NAME in (select t.TRIGGER_NAME from QRTZ_TRIGGERS t where t.JOB_NAME = 'BPMEventHandling')")[0] == 0
        sql.firstRow("select count(*) from QRTZ_FIRED_TRIGGERS where TRIGGER_NAME in (select t.TRIGGER_NAME from QRTZ_TRIGGERS t where t.JOB_NAME = 'BPMEventHandling')")[0] == 0
        sql.firstRow("select count(*) from QRTZ_TRIGGERS t where t.JOB_NAME = 'BPMEventHandling'")[0] == 0
        sql.firstRow("select count(*) from QRTZ_JOB_DETAILS t where t.JOB_NAME = 'BPMEventHandling'")[0] == 0
    }

    def "timers should still work"() {
        given:
        def client = new APIClient()
        client.login("install", "install")
        def id = client.processAPI.getProcessDefinitionId("processStartedOneTime", "1.0")
        when:
        def timeout = System.currentTimeMillis() + 3000
        def SearchResult<ArchivedProcessInstance> instances
        while ((instances = client.processAPI.searchArchivedProcessInstances(new SearchOptionsBuilder(0, 1).filter(ArchivedProcessInstancesSearchDescriptor.PROCESS_DEFINITION_ID, id).done())).count == 0 && System.currentTimeMillis() < timeout) {
            Thread.sleep(200)
        }
        then:
        instances.count > 0
    }

    def "messages should still match"() {
        given:
        def client = new APIClient()
        client.login("install", "install")
        def businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                new ProcessDefinitionBuilder()
                        .createNewInstance("sendMessage", "1.0")
                        .addEndEvent("sendTheMessage")
                        .addMessageEventTrigger("theMessage", new ExpressionBuilder().createConstantStringExpression("receiveTheMessage"), new ExpressionBuilder().createConstantStringExpression("startWithTheMessage"))
                        .getProcess())
        def sendProcessDefinition = client.processAPI.deploy(businessArchiveBuilder.done())
        client.processAPI.enableProcess(sendProcessDefinition.id)
        businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                new ProcessDefinitionBuilder()
                        .createNewInstance("receiveTheMessage", "1.0")
                        .addStartEvent("startWithTheMessage")
                        .addMessageEventTrigger("theMessage")
                        .addAutomaticTask("taskTriggeredByStartEvent")
                        .addTransition("startWithTheMessage", "taskTriggeredByStartEvent")
                        .getProcess())
        def receiveProcessDefinition = client.processAPI.deploy(businessArchiveBuilder.done())
        client.processAPI.enableProcess(receiveProcessDefinition.id)
        when:
        client.processAPI.startProcess(sendProcessDefinition.id)
        client.processAPI.startProcess(sendProcessDefinition.id)
        def timeout = System.currentTimeMillis() + 10000
        def SearchResult<ArchivedFlowNodeInstance> instances
        while ((instances = client.processAPI.searchArchivedFlowNodeInstances(new SearchOptionsBuilder(0, 1).filter(ArchivedFlowNodeInstanceSearchDescriptor.NAME, "taskTriggeredByStartEvent").done())).count == 0 && System.currentTimeMillis() < timeout) {
            Thread.sleep(200)
        }
        then:
        instances.count > 0
    }

}
