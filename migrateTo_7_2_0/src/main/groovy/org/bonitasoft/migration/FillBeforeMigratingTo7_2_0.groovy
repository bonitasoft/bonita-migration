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
import org.bonitasoft.engine.api.CommandAPI
import org.bonitasoft.engine.api.PlatformAPIAccessor
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter
import org.bonitasoft.engine.bdm.model.BusinessObject
import org.bonitasoft.engine.bdm.model.BusinessObjectModel
import org.bonitasoft.engine.bdm.model.Query
import org.bonitasoft.engine.bdm.model.field.FieldType
import org.bonitasoft.engine.bdm.model.field.SimpleField
import org.bonitasoft.engine.bpm.actor.ActorCriterion
import org.bonitasoft.engine.bpm.bar.BarResource
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder
import org.bonitasoft.engine.bpm.connector.ConnectorEvent
import org.bonitasoft.engine.bpm.contract.Type
import org.bonitasoft.engine.bpm.process.ProcessDefinition
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
import org.bonitasoft.engine.command.BusinessDataCommandField
import org.bonitasoft.engine.command.ExecuteBDMQueryCommand
import org.bonitasoft.engine.command.GetBusinessDataByQueryCommand
import org.bonitasoft.engine.expression.Expression
import org.bonitasoft.engine.expression.ExpressionBuilder
import org.bonitasoft.engine.operation.OperationBuilder
import org.bonitasoft.engine.test.PlatformTestUtil
import org.bonitasoft.migration.filler.*
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

    def createBusinessObjectModel() {
        final SimpleField firstName = new SimpleField()
        firstName.setName("firstName")
        firstName.setType(FieldType.STRING)
        firstName.setLength(Integer.valueOf(100))

        final SimpleField lastName = new SimpleField()
        lastName.setName("lastName")
        lastName.setType(FieldType.STRING)
        lastName.setLength(Integer.valueOf(100))
        lastName.setNullable(Boolean.FALSE)

        final SimpleField phoneNumbers = new SimpleField()
        phoneNumbers.setName("phoneNumbers")
        phoneNumbers.setType(FieldType.STRING)
        phoneNumbers.setLength(Integer.valueOf(10))
        phoneNumbers.setCollection(Boolean.TRUE)

        final SimpleField hireDate = new SimpleField()
        hireDate.setName("hireDate")
        hireDate.setType(FieldType.DATE)

        final SimpleField booleanField = new SimpleField()
        booleanField.setName("booleanField")
        booleanField.setType(FieldType.BOOLEAN)

        final BusinessObject employee = new BusinessObject()
        employee.setQualifiedName("com.company.model.Employee")
        employee.addField(hireDate)
        employee.addField(booleanField)
        employee.addField(firstName)
        employee.addField(lastName)
        employee.addField(phoneNumbers)
        employee.setDescription("Describe a simple employee")
        employee.addUniqueConstraint("uk_fl", "firstName", "lastName")

        final Query getEmployeeByPhoneNumber = employee.addQuery("findByPhoneNumber",
                "SELECT e FROM Employee e WHERE :phoneNumber IN ELEMENTS(e.phoneNumbers)", List.class.getName());
        getEmployeeByPhoneNumber.addQueryParameter("phoneNumber", String.class.getName());

        final Query findByFirstNAmeAndLastNameNewOrder = employee.addQuery("findByFirstNameAndLastNameNewOrder",
                "SELECT e FROM Employee e WHERE e.firstName =:firstName AND e.lastName = :lastName ORDER BY e.lastName", List.class.getName());
        findByFirstNAmeAndLastNameNewOrder.addQueryParameter("firstName", String.class.getName());
        findByFirstNAmeAndLastNameNewOrder.addQueryParameter("lastName", String.class.getName());


        final Query findByHireDate = employee.addQuery("findByHireDateRange",
                "SELECT e FROM Employee e WHERE e.hireDate >=:date1 and e.hireDate <=:date2", List.class.getName());
        findByHireDate.addQueryParameter("date1", Date.class.getName());
        findByHireDate.addQueryParameter("date2", Date.class.getName());

        employee.addQuery("countForAllEmployee", "SELECT COUNT(e) FROM Employee e", Long.class.getName());

        employee.addIndex("IDX_LSTNM", "lastName");

        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(employee)
        model
    }

    @FillerBdmInitializer
    def deployBDM() {
        def businessObjectModel=createBusinessObjectModel()
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install")
        def tenantAdministrationAPI = TenantAPIAccessor.getTenantAdministrationAPI(session)

        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter()
        final byte[] zip = converter.zip(businessObjectModel)

        tenantAdministrationAPI.pause()
        tenantAdministrationAPI.installBusinessDataModel(zip)
        tenantAdministrationAPI.resume()
    }

    @FillAction
    public void deployProcessWithBdm() {
        def loginAPI = TenantAPIAccessor.getLoginAPI()
        def session = loginAPI.login("install", "install")
        def identityAPI = TenantAPIAccessor.getIdentityAPI(session)
        def processAPI = TenantAPIAccessor.getProcessAPI(session)
        def commandAPI = TenantAPIAccessor.getCommandAPI(session)

        def user = identityAPI.createUser("userForBPMProcess", "bpm")

        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", new StringBuilder().append("import ")
                .append("com.company.model.Employee; ")
                .append("import java.util.Calendar.*; ")
                .append("Employee e = new Employee(); e.firstName = 'Jane'+Calendar.instance.time; e.lastName = 'Doe'; return e;").toString(),
                "com.company.model.Employee");

        def builder = new ProcessDefinitionBuilder()
        builder.createNewInstance("process with BDM", "before_7_2")
        builder.addActor("BDM actor", true)
        builder.addBusinessData("myEmployee", "com.company.model.Employee", employeeExpression)
        builder.addAutomaticTask("step1").addDescription("autoTaskDesc")

        def businessArchive = new BusinessArchiveBuilder()
                .createNewBusinessArchive()
                .setProcessDefinition(builder.getProcess())
                .done()

        def processDefinition = processAPI.deploy(businessArchive)
        addUserToProcessActors(processAPI, processDefinition, user)
        processAPI.enableProcess(processDefinition.id)
        processAPI.startProcess(user.getId(), processDefinition.id)

        callBdmQuery(commandAPI, "Employee.find", "com.company.model.Employee")
        callBusinessDataByQuery(commandAPI, "find", "com.company.model.Employee")

        loginAPI.logout(session)

    }

    private void callBdmQuery(CommandAPI commandAPI, String queryName, String returnType) {
        final Map<String, Serializable> parameters = new HashMap<>()
        parameters.put(ExecuteBDMQueryCommand.QUERY_NAME, queryName)
        parameters.put(ExecuteBDMQueryCommand.RETURN_TYPE, returnType)

        final Map<String, Serializable> queryParameters = new HashMap<>()
        parameters.put(ExecuteBDMQueryCommand.RETURNS_LIST, true)
        parameters.put(ExecuteBDMQueryCommand.START_INDEX, 0)
        parameters.put(ExecuteBDMQueryCommand.MAX_RESULTS, 10)

        parameters.put(queryParameters, (Serializable) queryParameters)
        def execute = commandAPI.execute("executeBDMQuery", parameters)

        displayCommandResult execute
    }

    private void callBusinessDataByQuery(CommandAPI commandAPI, String queryName, String returnType) {
        final Map<String, Serializable> parameters = new HashMap<>()
        parameters.put(GetBusinessDataByQueryCommand.QUERY_NAME, queryName)
        parameters.put(ExecuteBDMQueryCommand.RETURN_TYPE, returnType)

        final Map<String, Serializable> queryParameters = new HashMap<>()
        parameters.put(GetBusinessDataByQueryCommand.ENTITY_CLASS_NAME, returnType)
        parameters.put(GetBusinessDataByQueryCommand.START_INDEX, 0)
        parameters.put(GetBusinessDataByQueryCommand.MAX_RESULTS, 10)
        parameters.put(BusinessDataCommandField.BUSINESS_DATA_URI_PATTERN, "/businessdata/{className}/{id}/{field}")

        parameters.put(queryParameters, (Serializable) queryParameters)
        def execute = commandAPI.execute("getBusinessDataByQueryCommand", parameters)

        displayCommandResult execute
    }

    private displayCommandResult(Serializable execute) {
        def bytes = execute as byte[]
        def string = new String(bytes)
        println "Employee.find BDM query result:\n$string"

    }

    private void addUserToProcessActors(ProcessAPI processAPI, ProcessDefinition processDefinition, user) {
        def actors = processAPI.getActors(processDefinition.id, 0, 100, ActorCriterion.NAME_ASC)
        actors.each { processAPI.addUserToActor(it.id, user.id) }
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
                .addOutput(new OperationBuilder().createSetDataOperation("myData", new ExpressionBuilder().createInputExpression("outputValue",String.class.getName())))
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




    @FillAction
    public void barInDatabase() {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install");
        def processAPI = TenantAPIAccessor.getProcessAPI(session)
        def identityAPI = TenantAPIAccessor.getIdentityAPI(session)
        def user = identityAPI.createUser("userOfBARInDatabase", "bpm")

        def builder = new BusinessArchiveBuilder().createNewBusinessArchive()
        def processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("barInDatabase", "1.1.0")
        processDefinitionBuilder.addActor("theUser")
        def task = processDefinitionBuilder.addUserTask("step1", "theUser")
        task.addConnector("theConnector", "connectorId", "version", ConnectorEvent.ON_ENTER).addInput("input1", new ExpressionBuilder().createConstantStringExpression("input1Value"))
                .addOutput(new OperationBuilder().createSetDataOperation("myData", new ExpressionBuilder().createInputExpression("outputValue",String.class.getName())))
        processDefinitionBuilder.addUserTask("step2", "theUser")
        processDefinitionBuilder.addDocumentDefinition("myDoc1").addFile("initialContent1.txt").addContentFileName("MyFile.txt").addMimeType("plain/text")
        processDefinitionBuilder.addDocumentDefinition("myDoc2").addFile("initialContent2.txt").addContentFileName("MyFile.txt").addMimeType("plain/text")
        processDefinitionBuilder.addDocumentDefinition("myDoc3").addFile("initialContent3.txt").addContentFileName("MyFile.txt").addMimeType("plain/text")
        processDefinitionBuilder.addData("myData", "java.lang.String", new ExpressionBuilder().createConstantStringExpression("myDataValue")).addDescription("my data description")
        processDefinitionBuilder.addTransition("step1", "step2")

        builder.setProcessDefinition(processDefinitionBuilder.done())
        builder.addDocumentResource(new BarResource("initialContent1.txt", "This is the content of my file1".bytes))
        builder.addDocumentResource(new BarResource("initialContent2.txt", "This is the content of my file2".bytes))
        builder.addDocumentResource(new BarResource("initialContent3.txt", "This is the content of my file3".bytes))

        builder.setActorMapping("""
<actorMappings:actorMappings xmlns:actorMappings="http://www.bonitasoft.org/ns/actormapping/6.0">
    <actorMapping name="theUser">
        <users>
            <user>userOfBARInDatabase</user>
        </users>
        <groups />
        <roles />
        <memberships />
    </actorMapping>
</actorMappings:actorMappings>
""".getBytes())
        builder.addExternalResource(new BarResource("index.html", "<html>".getBytes()));
        builder.addExternalResource(new BarResource("content/other.html", "<html>1".getBytes()));
        builder.addUserFilters(new BarResource("MyUserFilter.impl", """
<connectorImplementation>
    <definitionId>connectorId</definitionId>
    <definitionVersion>version</definitionVersion>
    <implementationClassname>MyUserFilter</implementationClassname>
    <implementationId>implId</implementationId>
    <implementationVersion>1.0</implementationVersion>
    <jarDependencies>
    </jarDependencies>
</connectorImplementation>""".getBytes()))
        builder.addConnectorImplementation(new BarResource("myConnector.impl", """
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
