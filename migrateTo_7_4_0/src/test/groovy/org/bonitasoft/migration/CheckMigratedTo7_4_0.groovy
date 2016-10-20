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

import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.bpm.contract.Type
import org.bonitasoft.engine.bpm.flownode.*
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


}
