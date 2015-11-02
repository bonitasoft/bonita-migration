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
import org.bonitasoft.engine.bpm.bar.BarResource
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder
import org.bonitasoft.engine.bpm.connector.ConnectorEvent
import org.bonitasoft.engine.bpm.contract.Type
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
import org.bonitasoft.engine.connector.AbstractConnector
import org.bonitasoft.engine.connector.ConnectorException
import org.bonitasoft.engine.connector.ConnectorValidationException
import org.bonitasoft.engine.expression.ExpressionBuilder
import org.bonitasoft.engine.operation.OperationBuilder
import org.bonitasoft.engine.test.PlatformTestUtil
import org.bonitasoft.migration.filler.FillAction
import org.bonitasoft.migration.filler.FillerInitializer
import org.bonitasoft.migration.filler.FillerShutdown
import org.bonitasoft.migration.filler.FillerUtils

/**
 * @author Baptiste Mesta
 */
class FillBeforeMigratingTo7_2_0 {

    /**
     * init platform before fill actions
     */
    @FillerInitializer
    public void init() {
        FillerUtils.initializeEngineSystemProperties()
        LocalServerTestsInitializer.beforeAll();
    }

    public class MyConnector extends AbstractConnector {
        @Override
        protected void executeBusinessLogic() throws ConnectorException {
        }

        @Override
        void validateInputParameters() throws ConnectorValidationException {

        }
    }

    @FillAction
    public void deployProcessDefinitionXMLThatWillBeMigrated() {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install")

        def identityAPI = TenantAPIAccessor.getIdentityAPI(session)

        def user = identityAPI.createUser("userForMigratedProcess", "bpm")

        def builder = new ProcessDefinitionBuilder().createNewInstance("MyProcess to be migrated", "1.0-SNAPSHOT")
        builder.addDescription("2-lines\ndescription")
        builder.addDisplayDescription("2-lines\ndisplay description")
        builder.addAutomaticTask("step1").addDescription("autoTaskDesc")
        builder.addUserTask("step2", "myActor").addContract().addInput("myTaskContractInput", Type.BOOLEAN, "Serves description non-reg purposes")
        def task = builder.addAutomaticTask("taskWithNoDescription")
        task.addConnector("theConnector", "connectorId", "version", ConnectorEvent.ON_ENTER).addInput("input1", new ExpressionBuilder().createConstantStringExpression("input1Value"))
                .addOutput(new OperationBuilder().createSetDataOperation("myData", new ExpressionBuilder().createConstantStringExpression("outputValue")))
        task.addOperation(new OperationBuilder().createSetDataOperation("myData", new ExpressionBuilder().createConstantStringExpression("theNewValue")))
        builder.addTransition("step1", "step2")
        builder.addActor("myActor")
        builder.addData("myData", "java.lang.String", new ExpressionBuilder().createConstantStringExpression("myDataValue")).addDescription("my data description")
        builder.addXMLData("xmlData", new ExpressionBuilder().createGroovyScriptExpression("theScript", "'<tag>'+isOk+'</tag>'", String.class.getName(), new ExpressionBuilder().createContractInputExpression("isOk", List.class.getName()))).addDescription("xml data depends on myData")
        builder.addAutomaticTask("step3").addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(12));

        def contract = builder.addContract()
        contract.addInput("isOk", Type.BOOLEAN, "the is ok contract input", true);
        contract.addComplexInput("request", "a request", false).addInput("name", Type.TEXT, "name of the request").addInput("value", Type.INTEGER, "request amount")


        builder.setActorInitiator("myActorInitiator")

        def businessArchive = new BusinessArchiveBuilder()
                .createNewBusinessArchive()
                .setActorMapping("""<?xml version="1.0" encoding="UTF-8"?>
<actormappings:actorMappings xmlns:actormappings="http://www.bonitasoft.org/ns/actormapping/6.0">
\t<actorMapping name="myActor">
\t\t<users>
\t\t\t<user>userForMigratedProcess</user>
\t\t</users>
\t</actorMapping>
\t<actorMapping name="myActorInitiator">
\t\t<users>
\t\t\t<user>userForMigratedProcess</user>
\t\t</users>
\t</actorMapping>
</actormappings:actorMappings>""".getBytes())
                .setProcessDefinition(builder.getProcess())
                .addConnectorImplementation(new BarResource("myConnector.impl", """
<connectorImplementation>
    <definitionId>connectorId</definitionId>
    <definitionVersion>version</definitionVersion>
    <implementationClassname>${MyConnector.class.getName()}</implementationClassname>
    <implementationId>implId</implementationId>
    <implementationVersion>1.0</implementationVersion>
    <jarDependencies>
    </jarDependencies>
</connectorImplementation>""".getBytes()))
                .done()

        def processAPI = TenantAPIAccessor.getProcessAPI(session)
        def processDefinition = processAPI.deploy(businessArchive)

        println processAPI.getProcessResolutionProblems(processDefinition.id)
        processAPI.enableProcess(processDefinition.getId())

        def processInstance = processAPI.startProcessWithInputs(processDefinition.id, [isOk: [true, false, true], request: [name: "myRequest", value: 123]])

        TenantAPIAccessor.getLoginAPI().logout(session)
    }

    @FillAction
    public void parametersInDatabase() {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install");
        def processAPI = TenantAPIAccessor.getProcessAPI(session)
        def identityAPI = TenantAPIAccessor.getIdentityAPI(session)
        def user = identityAPI.createUser("userOfProcessWithParam", "bpm")

        def builder = new BusinessArchiveBuilder().createNewBusinessArchive()
        def processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithParameters", "1.1.0")
        processDefinitionBuilder.addActor("theUser")
        processDefinitionBuilder.addUserTask("step1", "theUser").addDisplayName(new ExpressionBuilder().createParameterExpression("myParam1", "myParam1", String.class.getName()))
        processDefinitionBuilder.addUserTask("step2", "theUser").addDisplayName(new ExpressionBuilder().createGroovyScriptExpression("theScript", "''+myParam2", String.class.getName(), new ExpressionBuilder().createParameterExpression("myParam2", "myParam2", Integer.class.getName())))
        processDefinitionBuilder.addParameter("myParam1", String.class.getName())
        processDefinitionBuilder.addParameter("myParam2", Integer.class.getName())
        processDefinitionBuilder.addParameter("myParam3", String.class.getName())



        builder.setProcessDefinition(processDefinitionBuilder.done())
        builder.setActorMapping("""
<actorMappings:actorMappings xmlns:actorMappings="http://www.bonitasoft.org/ns/actormapping/6.0">
    <actorMapping name="theUser">
        <users>
            <user>userOfProcessWithParam</user>
        </users>
        <groups />
        <roles />
        <memberships />
    </actorMapping>
</actorMappings:actorMappings>
""".getBytes())

        def theParam3Value = new byte[1150];
        Arrays.fill(theParam3Value, (byte) 65);
        builder.setParameters(["myParam1": "theParam1Value", "myParam2": "123456789", "myParam3": new String(theParam3Value)])

        def processDefinition = processAPI.deploy(builder.done())
        processAPI.enableProcess(processDefinition.getId())

        TenantAPIAccessor.getLoginAPI().logout(session)
    }

    /**
     * stop platform after all fill actions
     */
    @FillerShutdown
    public void shutdown() {
        new PlatformTestUtil().stopPlatformAndTenant(PlatformAPIAccessor.getPlatformAPI(new PlatformTestUtil().loginOnPlatform()), true)
    }

}
