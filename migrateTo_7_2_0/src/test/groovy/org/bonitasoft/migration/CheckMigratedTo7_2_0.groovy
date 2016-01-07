/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
import groovy.json.JsonSlurper
import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter
import org.bonitasoft.engine.bdm.model.BusinessObject
import org.bonitasoft.engine.bdm.model.BusinessObjectModel
import org.bonitasoft.engine.bdm.model.Query
import org.bonitasoft.engine.bdm.model.field.FieldType
import org.bonitasoft.engine.bdm.model.field.SimpleField
import org.bonitasoft.engine.bpm.bar.BusinessArchive
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory
import org.bonitasoft.engine.bpm.businessdata.BusinessDataQueryResult
import org.bonitasoft.engine.bpm.contract.Type
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion
import org.bonitasoft.engine.bpm.flownode.MultiInstanceLoopCharacteristics
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition
import org.bonitasoft.engine.bpm.parameter.ParameterInstance
import org.bonitasoft.engine.exception.NotFoundException
import org.bonitasoft.engine.command.BusinessDataCommandField
import org.bonitasoft.engine.command.GetBusinessDataByQueryCommand
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.bonitasoft.migration.filler.FillerUtils
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

import java.text.SimpleDateFormat

import static groovy.test.GroovyAssert.shouldFail
import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.tuple
/**
 * @author Laurent Leseigneur
 */
class CheckMigratedTo7_2_0 {

    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create().reuseExistingPlatform()


    @BeforeClass
    public static void beforeClass() {
        FillerUtils.initializeEngineSystemProperties()
    }

    @Test
    def void checkProcessStillWorks() {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install")
        def processAPI = TenantAPIAccessor.getProcessAPI(session)
        def id = processAPI.getProcessDefinitionId("MyProcess to be migrated", "1.0-SNAPSHOT")


        def definition = processAPI.getDesignProcessDefinition(id)

        assert definition.getName() == "MyProcess to be migrated"
        assert definition.getVersion() == "1.0-SNAPSHOT"
        assert definition.description == "2-lines\ndescription"
        assert definition.displayDescription == "2-lines\ndisplay description"


        def dataDefinition = definition.getFlowElementContainer().getDataDefinitions().get(0)
        assert dataDefinition.name == "myData"
        assert dataDefinition.description == "my data description"

        def step1 = definition.getFlowElementContainer().getActivity("step1")
        def step2 = definition.getFlowElementContainer().getActivity("step2")

        assert step1 != null
        assert step1.getOutgoingTransitions().size() == 1
        assert step1.getOutgoingTransitions().get(0).getTarget() == step2.getId()
        assert step1.description == "autoTaskDesc"

        assert step2 != null
        assert step2.getIncomingTransitions().size() == 1
        assert step2.getIncomingTransitions().get(0).getSource() == step1.getId()
        assert ((UserTaskDefinition) step2).getContract().getInputs().get(0).description == "Serves description non-reg purposes"

        // Check that we do not add a sub-element 'description' if there was no attribute 'description':
        def autoTask3 = definition.getFlowElementContainer().getActivity("taskWithNoDescription")
        assert autoTask3.description == null
        assert autoTask3.getConnectors().size() == 1
        assert autoTask3.getConnectors().get(0).getName() == "theConnector"
        assert autoTask3.getConnectors().get(0).getInputs().size() == 1
        assert autoTask3.getConnectors().get(0).getInputs().get("input1").getName() == "input1Value"
        assert autoTask3.getConnectors().get(0).getOutputs().size() == 1
        assert autoTask3.getOperations().size() == 1
        assert autoTask3.getOperations().get(0).getLeftOperand().name == "myData"

        assert definition.getFlowElementContainer().getTransitions().size() == 1
        assert definition.getFlowElementContainer().getTransitions().iterator().next().getSource() == step1.getId()
        assert definition.getFlowElementContainer().getTransitions().iterator().next().getTarget() == step2.getId()

        assert definition.getActorInitiator().getName() == "myActorInitiator"
        assert definition.getActorsList().size() == 2
        assert definition.getActorsList().get(0).getName() == "myActor"
        assert definition.getActorsList().get(1).getName() == "myActorInitiator"

        assert definition.getFlowElementContainer().getDataDefinition("xmlData") != null
        assert definition.getFlowElementContainer().getDataDefinition("xmlData").class.name.contains("XMLDataDefinition")
        assert definition.getFlowElementContainer().getDataDefinition("xmlData").getDefaultValueExpression().getContent() == "'<tag>'+isOk+'</tag>'"
        assert definition.getFlowElementContainer().getDataDefinition("xmlData").getDefaultValueExpression().getDependencies().size() == 1


        def step3 = definition.getFlowElementContainer().getActivity("step3")
        assert step3.loopCharacteristics instanceof MultiInstanceLoopCharacteristics
        assert !(step3.loopCharacteristics as MultiInstanceLoopCharacteristics).sequential
        assert (step3.loopCharacteristics as MultiInstanceLoopCharacteristics).loopCardinality.content == "12"

        assert definition.contract.inputs.size() == 2
        assert definition.contract.inputs[0].multiple
        assert definition.contract.inputs[0].type == Type.BOOLEAN
        assert definition.contract.inputs[0].name == "isOk"
        assert definition.contract.inputs[0].description == "the is ok contract input"
        assert definition.contract.inputs[1].hasChildren()
        assert definition.contract.inputs[1].inputs[0].name == "name"
        assert definition.contract.inputs[1].inputs[1].name == "value"
        assert definition.contract.inputs[1].inputs[1].type == Type.INTEGER



        def processInstance = processAPI.startProcessWithInputs(id, [isOk: [true, false, true], request: [name: "myRequest", value: 123]])

        def instances
        def timeout = System.currentTimeMillis() + 15000
        while ((instances = processAPI.getOpenActivityInstances(processInstance.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT)).size() < 1 && System.currentTimeMillis() < timeout) {
            Thread.sleep(200)
            println "wait 200"
        }
        assert instances.size() == 1
        assert instances.get(0).getName() == "step2"


        TenantAPIAccessor.getLoginAPI().logout(session)
    }


    @Test
    public void verifyParametersAreMigratedInDb() {
        def session = TenantAPIAccessor.getLoginAPI().login("userOfProcessWithParam", "bpm");
        def processAPI = TenantAPIAccessor.getProcessAPI(session)
        def processDefinitionId = processAPI.getProcessDefinitionId("processWithParameters", "1.1.0")

        processAPI.startProcess(processDefinitionId)
        def timeout = System.currentTimeMillis() + 15000
        def instances
        while ((instances = processAPI.getPendingHumanTaskInstances(session.getUserId(), 0, 10, ActivityInstanceCriterion.NAME_ASC)).size() < 2 && System.currentTimeMillis() < timeout) {
            Thread.sleep(200)
        }
        assertThat(instances).extracting("name", "displayName").containsExactly(tuple("step1", "theParam1Value"), tuple("step2", "123456789"))

        ParameterInstance parameterInstance = processAPI.getParameterInstance(processDefinitionId, "myParam3")
        assert ((String) parameterInstance.getValue()).length() == 1150
        def theParam3Value = new byte[1150];
        Arrays.fill(theParam3Value, (byte) 65);
        assert parameterInstance.getValue() == new String(theParam3Value)

        TenantAPIAccessor.getLoginAPI().logout(session)
    }


    @Test
    public void verify_BDM_businessData_query_call() {
        def queryName = "find"
        def returnType = "com.company.model.Employee"


        startProcessWithBdm()
        callBusinessDataByQuery(queryName, returnType, 2, false)

        redeployBdmAfterMigration(createBusinessObjectModel())

        startProcessWithBdm()
        callBusinessDataByQuery(queryName, returnType, 1, true)

    }

    private void startProcessWithBdm() {
        def loginAPI = TenantAPIAccessor.getLoginAPI()
        def session = loginAPI.login("userForBPMProcess", "bpm")
        def processAPI = TenantAPIAccessor.getProcessAPI(session)
        def processDefinitionId = processAPI.getProcessDefinitionId("process with BDM", "before_7_2")
        processAPI.startProcess(processDefinitionId)
        loginAPI.logout(session)

    }


    private void redeployBdmAfterMigration(BusinessObjectModel businessObjectModel) throws Exception {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install")
        def tenantAdministrationAPI = TenantAPIAccessor.getTenantAdministrationAPI(session)

        tenantAdministrationAPI.clientBDMZip

        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter()
        final byte[] zip = converter.zip(businessObjectModel)

        tenantAdministrationAPI.pause()
        def versionBefore = tenantAdministrationAPI.businessDataModelVersion
        tenantAdministrationAPI.cleanAndUninstallBusinessDataModel()
        tenantAdministrationAPI.resume()

        tenantAdministrationAPI.pause()
        def businessDataModelVersionFromInstall = tenantAdministrationAPI.installBusinessDataModel(zip)
        tenantAdministrationAPI.resume()
        def businessDataModelVersion = tenantAdministrationAPI.businessDataModelVersion

        assertThat(businessDataModelVersionFromInstall)
                .as("should redeploy BDM " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date()))
                .isNotNull()
                .isNotEqualTo(versionBefore)

        assertThat(businessDataModelVersion)
                .isNotNull()
                .isEqualTo(businessDataModelVersionFromInstall)

        TenantAPIAccessor.getLoginAPI().logout(session)

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


    private void callBusinessDataByQuery(String queryName, String returnType, Integer expectedSize, boolean expectBusinessDataQueryMetadata) {
        def session = TenantAPIAccessor.getLoginAPI().login("userForBPMProcess", "bpm")
        def commandAPI = TenantAPIAccessor.getCommandAPI(session)

        final Map<String, Serializable> parameters = new HashMap<>()
        parameters.put(GetBusinessDataByQueryCommand.QUERY_NAME, queryName)

        final Map<String, Serializable> queryParameters = new HashMap<>()
        parameters.put(GetBusinessDataByQueryCommand.ENTITY_CLASS_NAME, returnType)
        parameters.put(GetBusinessDataByQueryCommand.START_INDEX, 0)
        parameters.put(GetBusinessDataByQueryCommand.MAX_RESULTS, 10)
        parameters.put(BusinessDataCommandField.BUSINESS_DATA_URI_PATTERN, "/businessdata/{className}/{id}/{field}")

        parameters.put(queryParameters, (Serializable) queryParameters)

        def execute = commandAPI.execute("getBusinessDataByQueryCommand", parameters) as BusinessDataQueryResult
        if (expectBusinessDataQueryMetadata) {
            assertThat(execute.businessDataQueryMetadata.count).as("expect to have $expectedSize results").isEqualTo(expectedSize)
        }
        def slurper = new JsonSlurper()
        def result = slurper.parseText(execute.jsonResults)

        println "Employee.find BDM query result:\n${execute.jsonResults}"

        assertThat(result.size()).as("expect to have $expectedSize results").isEqualTo(expectedSize)
        assertThat(result[0].lastName).as("should have last name in ${result[0]}").isEqualTo("Doe")

    }



    @Test
    public void verifyBARsAreMigratedInDb() {
        def session = TenantAPIAccessor.getLoginAPI().login("userOfBARInDatabase", "bpm");
        def processAPI = TenantAPIAccessor.getProcessAPI(session)
        def processDefinitionId = processAPI.getProcessDefinitionId("barInDatabase", "1.1.0")

        def processInstance = processAPI.startProcess(processDefinitionId)
        def timeout = System.currentTimeMillis() + 15000
        while (processAPI.getPendingHumanTaskInstances(session.getUserId(), 0, 10, ActivityInstanceCriterion.NAME_ASC).size() < 1 && System.currentTimeMillis() < timeout) {
            Thread.sleep(200)
        }

        def instances = processAPI.getPendingHumanTaskInstances(session.getUserId(), 0, 10, ActivityInstanceCriterion.NAME_ASC)
        assertThat(instances).hasSize(1)
        assertThat(instances.get(0).getState()).isNotEqualTo("failed")

        def document = processAPI.getLastDocument(processInstance.id, "myDoc1")
        def content = processAPI.getDocumentContent(document.getContentStorageId())
        assertThat(new String(content)).isEqualTo("This is the content of my file1")
        assertThat(processAPI.getProcessDataInstance("myData", processInstance.getId()).getValue()).isEqualTo("input1Value")
        //export
        final byte[] bytes = processAPI.exportBarProcessContentUnderHome(processDefinitionId);
        final BusinessArchive exportedBAR = BusinessArchiveFactory.readBusinessArchive(new ByteArrayInputStream(bytes));

        //check
        def resources = exportedBAR.getResources()
        assertThat(resources).containsKeys(
                "documents/initialContent1.txt",
                "documents/initialContent2.txt",
                "documents/initialContent3.txt",
                "resources/index.html",
                "resources/content/other.html",
                "userFilters/MyUserFilter.impl",
                "connector/myConnector.impl"
        )
        assertThat(resources.get("documents/initialContent1.txt")).isEqualTo("This is the content of my file1".bytes)
        assertThat(resources.get("documents/initialContent2.txt")).isEqualTo("This is the content of my file2".bytes)
        assertThat(resources.get("documents/initialContent3.txt")).isEqualTo("This is the content of my file3".bytes)
        assertThat(resources.get("resources/index.html")).isEqualTo("<html>".bytes)
        assertThat(resources.get("resources/content/other.html")).isEqualTo("<html>1".bytes)
        assertThat(new String(resources.get("connector/myConnector.impl"))).contains(MyConnector.class.getName())
        assertThat(new String(resources.get("userFilters/MyUserFilter.impl"))).contains("MyUserFilter")
        TenantAPIAccessor.getLoginAPI().logout(session)
    }



    @Test
    public void checkFormMappingAreMigrated() {
        def session = TenantAPIAccessor.getLoginAPI().login("formMappingUser", "bpm");
        def pageAPI = TenantAPIAccessor.getCustomPageAPI(session)
        def page = pageAPI.getPageByName("custompage_mypage")


        def urlMapping = pageAPI.resolvePageOrURL("processInstance/formMappingProcess/1.1.0", ["IS_ADMIN": true], true)
        assert urlMapping.url == "the url of the page" + getUrlAddition()
        def internalMapping = pageAPI.resolvePageOrURL("process/formMappingProcess/1.1.0", ["IS_ADMIN": true], true);
        assert internalMapping.pageId == page.id
        def noneMapping = pageAPI.resolvePageOrURL("taskInstance/formMappingProcess/1.1.0/step1", ["IS_ADMIN": true], true)
        assert noneMapping.pageId == null;
        assert noneMapping.url == null;
        shouldFail(NotFoundException) {
            assert pageAPI.resolvePageOrURL("taskInstance/formMappingProcess/1.1.0/step2", ["IS_ADMIN": true], true);
        }

        TenantAPIAccessor.getLoginAPI().logout(session)
    }

    protected String getUrlAddition() {
        ""
    }


}