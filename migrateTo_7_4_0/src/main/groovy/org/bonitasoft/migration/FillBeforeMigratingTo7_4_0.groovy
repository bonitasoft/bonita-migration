/**
 * Copyright (C) 2016 BonitaSoft S.A.
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

import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter
import org.bonitasoft.engine.bdm.model.BusinessObject
import org.bonitasoft.engine.bdm.model.BusinessObjectModel
import org.bonitasoft.engine.bdm.model.field.FieldType
import org.bonitasoft.engine.bdm.model.field.SimpleField
import org.bonitasoft.engine.bpm.bar.BarResource
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping
import org.bonitasoft.engine.bpm.connector.ConnectorEvent
import org.bonitasoft.engine.bpm.contract.Type
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion
import org.bonitasoft.engine.bpm.flownode.GatewayType
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.TimerType
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
import org.bonitasoft.engine.expression.ExpressionBuilder
import org.bonitasoft.engine.operation.OperationBuilder
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.bonitasoft.migration.filler.FillAction
import org.bonitasoft.migration.filler.FillerBdmInitializer
import org.bonitasoft.migration.filler.FillerInitializer
import org.bonitasoft.migration.filler.FillerUtils
import org.junit.Rule

/**
 * @author Emmanuel Duchastenier
 */
class FillBeforeMigratingTo7_4_0 {

    public static final long ONE_HOUR = 1000L * 60L * 60L

    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create().keepPlatformOnShutdown()

    @FillerInitializer
    public void init() {
        FillerUtils.initializeEngineSystemProperties()
    }

    def static BusinessObjectModel createBusinessObjectModel() {
        final SimpleField firstName = new SimpleField()
        firstName.setName("name")
        firstName.setType(FieldType.STRING)
        firstName.setLength(Integer.valueOf(100))
        final BusinessObject testEntity = new BusinessObject()
        testEntity.setQualifiedName("com.company.model.TestEntity")
        testEntity.addField(firstName)
        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(testEntity)
        model
    }

    @FillerBdmInitializer
    def deployBDM() {
        def businessObjectModel = createBusinessObjectModel()
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install")
        def tenantAdministrationAPI = TenantAPIAccessor.getTenantAdministrationAPI(session)

        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter()
        final byte[] zip = converter.zip(businessObjectModel)

        tenantAdministrationAPI.pause()
        tenantAdministrationAPI.installBusinessDataModel(zip)
        tenantAdministrationAPI.resume()
    }

    @FillAction
    public void deployProcessDefinitionXMLThatWillBeMigrated() {
        def loginAPI = TenantAPIAccessor.getLoginAPI()
        def session = loginAPI.login("install", "install")
        def identityAPI = TenantAPIAccessor.getIdentityAPI(session)

        def user = identityAPI.createUser("userForMigratedProcess", "bpm")
        loginAPI.login("userForMigratedProcess", "bpm")


        def builder = new ProcessDefinitionBuilder().createNewInstance("MyProcess to be migrated", "to_7.4")
        builder.addDescription("2-lines\ndescription")
        builder.addDisplayDescription("2-lines\ndisplay description")
        builder.addAutomaticTask("auto1").addDescription("autoTaskDesc")
        builder.addUserTask("user1", "myActor").addExpectedDuration(ONE_HOUR).addContract().addInput("myTaskContractInput", Type.BOOLEAN, "Serves description non-reg purposes")
        def task = builder.addAutomaticTask("taskWithNoDescription")
        task.addConnector("theConnector", "connectorId", "version", ConnectorEvent.ON_ENTER).addInput("input1", new ExpressionBuilder().createConstantStringExpression("input1Value"))
                .addOutput(new OperationBuilder().createSetDataOperation("myData", new ExpressionBuilder().createInputExpression("outputValue", String.class.getName())))
        task.addOperation(new OperationBuilder().createSetDataOperation("myData", new ExpressionBuilder().createConstantStringExpression("theNewValue")))
        def unknown = new ExpressionBuilder().createConstantStringExpression("unknown")
        builder.addCallActivity("call", unknown, unknown).addProcessStartContractInput("theInput", new ExpressionBuilder().createConstantStringExpression("theValue"))
        builder.addAutomaticTask("auto2")
        builder.addAutomaticTask("auto3")

        builder.addUserTask("user2", "myActor").addDescription("without expectedDuration")
        builder.addManualTask("manual1", "myActor").addExpectedDuration(ONE_HOUR).addDescription("with expectedDuration")
        builder.addManualTask("manual2", "myActor").addDescription("with expectedDuration")

        builder.addGateway("gate1", GatewayType.INCLUSIVE)
        builder.addStartEvent("start")
                .addDescription("start event description")
                .addDisplayDescription(new ExpressionBuilder().createConstantStringExpression("start event display description"))
                .addDisplayDescriptionAfterCompletion(new ExpressionBuilder().createConstantStringExpression("start event after completion display description"))

        builder.addEndEvent("end")
                .addDescription("end event description")
                .addDisplayDescription(new ExpressionBuilder().createConstantStringExpression("end event display description"))
                .addDisplayDescriptionAfterCompletion(new ExpressionBuilder().createConstantStringExpression("end event after completion display description"))

        builder.addTransition("start", "auto1")
        builder.addTransition("auto1", "user1")
        builder.addTransition("user1", "gate1")
        def falseExpr = new ExpressionBuilder().createConstantBooleanExpression(false)
        builder.addTransition("gate1", "call", falseExpr)
        builder.addTransition("gate1", "auto2", falseExpr)
        builder.addDefaultTransition("gate1", "auto3")
        builder.addTransition("call", "end")
        builder.addTransition("auto2", "end")
        builder.addTransition("auto3", "end")

        builder.addActor("myActor")
        builder.setActorInitiator("myActorInitiator")
        builder.addData("myData", "java.lang.String", new ExpressionBuilder().createConstantStringExpression("myDataValue")).addDescription("my data description")
        builder.addXMLData("xmlData", new ExpressionBuilder().createGroovyScriptExpression("theScript", "'<tag>'+isOk+'</tag>'", String.class.getName(),
                new ExpressionBuilder().createContractInputExpression("isOk", List.class.getName()))).addDescription("xml data depends on myData")
        def integerExpression = new ExpressionBuilder().createConstantIntegerExpression(12)
        builder.addAutomaticTask("auto4").addMultiInstance(false, integerExpression)
        builder.addBusinessData("myBizData", "com.company.model.TestEntity", new ExpressionBuilder().createGroovyScriptExpression("createBusinessData",
                """
def testEntity = new com.company.model.TestEntity()
testEntity.name = 'toto'
return testEntity
""", "com.company.model.TestEntity"))

        def contract = builder.addContract()
        contract.addInput("isOk", Type.BOOLEAN, "the is ok contract input", true)
        contract.addComplexInput("request", "a request", false).addInput("name", Type.TEXT, "name of the request").addInput("value", Type.INTEGER, "request amount")

        def actor = new Actor("myActor")
        actor.addUser(user.getUserName())
        def actorMapping = new ActorMapping()
        actorMapping.addActor(actor)
        def actor1 = new Actor("myActorInitiator")
        actor1.addUser(user.getUserName())
        actorMapping.addActor(actor1)
        def businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive()
        businessArchiveBuilder.addConnectorImplementation(new BarResource("connector.impl",
                """
<connectorImplementation>
    <definitionId>connectorId</definitionId>
    <definitionVersion>version</definitionVersion>
    <implementationClassname>org.bonitasoft.migration.MyConnector</implementationClassname>
    <implementationId>connectorId</implementationId>
    <implementationVersion>version</implementationVersion>
    <jarDependencies></jarDependencies>
</connectorImplementation>
        """.getBytes()))
        def businessArchive = businessArchiveBuilder
                .setActorMapping(actorMapping)
                .setProcessDefinition(builder.getProcess())
                .done()

        def processAPI = TenantAPIAccessor.getProcessAPI(session)
        def processDefition = processAPI.deploy(businessArchive)

        processAPI.getProcessResolutionProblems(processDefition.id).each {
            println it.description
        }
        processAPI.enableProcess(processDefition.id)
        processAPI.startProcessWithInputs(processDefition.id, [isOk: [true], request: [name: 'theName', value: 1]])

        def HumanTaskInstance[] instances
        def timeout = System.currentTimeMillis() + 3000
        while ((instances = processAPI.getPendingHumanTaskInstances(session.userId, 0, 10, ActivityInstanceCriterion.DEFAULT)).size() < 1 && System.currentTimeMillis() < timeout) {
            Thread.sleep(200)
        }
        loginAPI.logout(session)
    }


    @FillAction
    public void "create process with a start timer to check quartz still works after migration"() {
        def client = new APIClient()
        client.login("install", "install")
        def builder = new ProcessDefinitionBuilder().createNewInstance("processStartedOneTime", "1.0")
        builder.addStartEvent("startWithTimer").addTimerEventTriggerDefinition(TimerType.DATE, new ExpressionBuilder().createGroovyScriptExpression("current plus 10 sec", "new java.util.Date(System.currentTimeMillis() + 10000)", "java.util.Date"))
        def businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(builder.done()).done()
        def processDefinition = client.processAPI.deploy(businessArchive)
        client.processAPI.enableProcess(processDefinition.getId())
    }

}

