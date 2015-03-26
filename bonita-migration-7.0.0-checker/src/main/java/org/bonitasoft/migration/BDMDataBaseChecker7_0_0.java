package org.bonitasoft.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.test.APITestUtil;

/**
 * @author Laurent Leseigneur
 */
public class BDMDataBaseChecker7_0_0 {

    public static final String PROCESS_WITH_BDM_NAME = "processWithBdm";

    protected static final String BDM_PACKAGE_PREFIX = "com.company.model";

    protected static final String COUNTRY_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Country";

    protected static final String ADDRESS_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Address";

    protected static final String EMPLOYEE_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Employee";

    protected static final String PRODUCT_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Product";

    protected static final String PRODUCT_CATALOG_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".ProductCatalog";

    protected static final String PERSON_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Person";

    protected static final String GET_EMPLOYEE_BY_LAST_NAME_QUERY_NAME = "findByLastName";

    protected static final String GET_EMPLOYEE_BY_PHONE_NUMBER_QUERY_NAME = "findByPhoneNumber";

    protected static final String FIND_BY_FIRST_NAME_FETCH_ADDRESSES = "findByFirstNameFetchAddresses";

    protected static final String FIND_BY_FIRST_NAME_AND_LAST_NAME_NEW_ORDER = "findByFirstNameAndLastNameNewOrder";

    protected static final String COUNT_EMPLOYEE = "countEmployee";

    protected static final String FIND_BY_HIRE_DATE_RANGE = "findByHireDateRange";

    protected static final String ACTOR_NAME = "Employee actor";
    public static final String STEP_1 = "bdmProcessStep1";
    public static final String STEP_2 = "bdmProcessStep2";

    public void should_deploy_a_business_data_model(APITestUtil apiTestUtil, User user) throws Exception {

        // before
        setUp_BDM(apiTestUtil);

        // test itself
        deployABDRAndCreateADefaultBusinessDataAndReuseReference(apiTestUtil, user);

        // after
        tearDown_BDM(apiTestUtil);
    }

    protected void deployABDRAndCreateADefaultBusinessDataAndReuseReference(APITestUtil apiTestUtil, User user) throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", new StringBuilder().append("import ")
                .append(EMPLOYEE_QUALIFIED_NAME).append("; Employee e = new Employee(); e.firstName = 'Jane'; e.lastName = 'Doe'; return e;").toString(),
                EMPLOYEE_QUALIFIED_NAME);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIFIED_NAME, employeeExpression);
        final String secondBizData = "people";
        processDefinitionBuilder.addBusinessData(secondBizData, EMPLOYEE_QUALIFIED_NAME, null);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask(STEP_1, ACTOR_NAME).addOperation(
                new OperationBuilder().attachBusinessDataSetAttributeOperation(secondBizData, new ExpressionBuilder().createQueryBusinessDataExpression(
                        "oneEmployee", "Employee." + GET_EMPLOYEE_BY_LAST_NAME_QUERY_NAME, EMPLOYEE_QUALIFIED_NAME,
                        new ExpressionBuilder().createConstantStringExpression("lastName", "Doe"))));
        processDefinitionBuilder.addUserTask(STEP_2, ACTOR_NAME);
        processDefinitionBuilder.addTransition(STEP_1, STEP_2);

        final ProcessDefinition definition = apiTestUtil.deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = apiTestUtil.getProcessAPI().startProcess(definition.getId());

        final long stepId = apiTestUtil.waitForUserTask(processInstance.getId(), STEP_1);
        final String employeeToString = getEmployeeToString(apiTestUtil, "myEmployee", processInstance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=Jane, lastName=Doe]");

        apiTestUtil.assignAndExecuteStep(stepId, user);
        apiTestUtil.waitForUserTask(processInstance, STEP_2);
        final String people = getEmployeeToString(apiTestUtil, secondBizData, processInstance.getId());
        assertThat(people).isEqualTo("Employee [firstName=Jane, lastName=Doe]");

        apiTestUtil.disableAndDeleteProcess(definition.getId());
    }

    protected String getEmployeeToString(APITestUtil apiTestUtil, final String businessDataName, final long processInstanceId)
            throws InvalidExpressionException {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(5);
        final String expressionEmployee = "retrieve_Employee";
        expressions.put(
                new ExpressionBuilder().createGroovyScriptExpression(expressionEmployee, "\"Employee [firstName=\" + " + businessDataName
                        + ".firstName + \", lastName=\" + " + businessDataName + ".lastName + \"]\";", String.class.getName(),
                        new ExpressionBuilder().createBusinessDataExpression(businessDataName, EMPLOYEE_QUALIFIED_NAME)), null);
        try {
            final Map<String, Serializable> evaluatedExpressions = apiTestUtil.getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId,
                    expressions);
            return (String) evaluatedExpressions.get(expressionEmployee);
        } catch (final ExpressionEvaluationException eee) {
            System.err.println(eee.getMessage());
            return null;
        }
    }

    protected void setUp_BDM(APITestUtil apiTestUtil) throws Exception {
        TenantAdministrationAPI tenantAdministrationAPI = TenantAPIAccessor.getTenantAdministrationAPI(apiTestUtil.getSession());

        final byte[] zip = new BusinessObjectModelConverter().zip(buildBOM());
        tenantAdministrationAPI.pause();
        tenantAdministrationAPI.installBusinessDataModel(zip);
        tenantAdministrationAPI.resume();
    }

    protected void tearDown_BDM(APITestUtil apiTestUtil) throws Exception {
        TenantAdministrationAPI tenantAdministrationAPI = TenantAPIAccessor.getTenantAdministrationAPI(apiTestUtil.getSession());

        if (!tenantAdministrationAPI.isPaused()) {
            tenantAdministrationAPI.pause();
            tenantAdministrationAPI.cleanAndUninstallBusinessDataModel();
            tenantAdministrationAPI.resume();
        }
    }

    protected BusinessObjectModel buildBOM() {
        final SimpleField name = new SimpleField();
        name.setName("name");
        name.setType(FieldType.STRING);

        final BusinessObject countryBO = new BusinessObject();
        countryBO.setQualifiedName(COUNTRY_QUALIFIED_NAME);
        countryBO.addField(name);

        final SimpleField street = new SimpleField();
        street.setName("street");
        street.setType(FieldType.STRING);

        final SimpleField city = new SimpleField();
        city.setName("city");
        city.setType(FieldType.STRING);

        final RelationField country = new RelationField();
        country.setType(RelationField.Type.AGGREGATION);
        country.setFetchType(RelationField.FetchType.LAZY);
        country.setName("country");
        country.setCollection(Boolean.FALSE);
        country.setNullable(Boolean.TRUE);
        country.setReference(countryBO);

        final BusinessObject addressBO = new BusinessObject();
        addressBO.setQualifiedName(ADDRESS_QUALIFIED_NAME);
        addressBO.addField(street);
        addressBO.addField(city);
        addressBO.addField(country);

        final RelationField addresses = new RelationField();
        addresses.setType(RelationField.Type.AGGREGATION);
        addresses.setFetchType(RelationField.FetchType.EAGER);
        addresses.setName("addresses");
        addresses.setCollection(Boolean.TRUE);
        addresses.setNullable(Boolean.TRUE);
        addresses.setReference(addressBO);

        final RelationField address = new RelationField();
        address.setType(RelationField.Type.AGGREGATION);
        address.setFetchType(RelationField.FetchType.LAZY);
        address.setName("address");
        address.setCollection(Boolean.FALSE);
        address.setNullable(Boolean.TRUE);
        address.setReference(addressBO);

        final SimpleField firstName = new SimpleField();
        firstName.setName("firstName");
        firstName.setType(FieldType.STRING);
        firstName.setLength(Integer.valueOf(10));

        final SimpleField lastName = new SimpleField();
        lastName.setName("lastName");
        lastName.setType(FieldType.STRING);
        lastName.setNullable(Boolean.FALSE);

        final SimpleField phoneNumbers = new SimpleField();
        phoneNumbers.setName("phoneNumbers");
        phoneNumbers.setType(FieldType.STRING);
        phoneNumbers.setLength(Integer.valueOf(10));
        phoneNumbers.setCollection(Boolean.TRUE);

        final SimpleField hireDate = new SimpleField();
        hireDate.setName("hireDate");
        hireDate.setType(FieldType.DATE);

        final SimpleField booleanField = new SimpleField();
        booleanField.setName("booleanField");
        booleanField.setType(FieldType.BOOLEAN);

        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        employee.addField(hireDate);
        employee.addField(booleanField);
        employee.addField(firstName);
        employee.addField(lastName);
        employee.addField(phoneNumbers);
        employee.addField(addresses);
        employee.addField(address);
        employee.setDescription("Describe a simple employee");
        employee.addUniqueConstraint("uk_fl", "firstName", "lastName");

        final Query getEmployeeByPhoneNumber = employee.addQuery(GET_EMPLOYEE_BY_PHONE_NUMBER_QUERY_NAME,
                "SELECT e FROM Employee e WHERE :phoneNumber IN ELEMENTS(e.phoneNumbers)", List.class.getName());
        getEmployeeByPhoneNumber.addQueryParameter("phoneNumber", String.class.getName());

        final Query findByFirstNAmeAndLastNameNewOrder = employee.addQuery(FIND_BY_FIRST_NAME_AND_LAST_NAME_NEW_ORDER,
                "SELECT e FROM Employee e WHERE e.firstName =:firstName AND e.lastName = :lastName ORDER BY e.lastName", List.class.getName());
        findByFirstNAmeAndLastNameNewOrder.addQueryParameter("firstName", String.class.getName());
        findByFirstNAmeAndLastNameNewOrder.addQueryParameter("lastName", String.class.getName());

        final Query findByFirstNameFetchAddresses = employee.addQuery(FIND_BY_FIRST_NAME_FETCH_ADDRESSES,
                "SELECT e FROM Employee e INNER JOIN FETCH e.addresses WHERE e.firstName =:firstName ORDER BY e.lastName", List.class.getName());
        findByFirstNameFetchAddresses.addQueryParameter("firstName", String.class.getName());

        final Query findByHireDate = employee.addQuery(FIND_BY_HIRE_DATE_RANGE,
                "SELECT e FROM Employee e WHERE e.hireDate >=:date1 and e.hireDate <=:date2", List.class.getName());
        findByHireDate.addQueryParameter("date1", Date.class.getName());
        findByHireDate.addQueryParameter("date2", Date.class.getName());

        employee.addQuery(COUNT_EMPLOYEE, "SELECT COUNT(e) FROM Employee e", Long.class.getName());

        employee.addIndex("IDX_LSTNM", "lastName");

        final BusinessObject person = new BusinessObject();
        person.setQualifiedName(PERSON_QUALIFIED_NAME);
        person.addField(hireDate);
        person.addField(firstName);
        person.addField(lastName);
        person.addField(phoneNumbers);
        person.setDescription("Describe a simple person");
        person.addUniqueConstraint("uk_fl", "firstName", "lastName");

        final BusinessObject productBO = new BusinessObject();
        productBO.setQualifiedName(PRODUCT_QUALIFIED_NAME);
        productBO.addField(name);

        final RelationField products = new RelationField();
        products.setType(RelationField.Type.AGGREGATION);
        products.setFetchType(RelationField.FetchType.LAZY);
        products.setName("products");
        products.setCollection(Boolean.TRUE);
        products.setNullable(Boolean.TRUE);
        products.setReference(productBO);

        final BusinessObject catalogBO = new BusinessObject();
        catalogBO.setQualifiedName(PRODUCT_CATALOG_QUALIFIED_NAME);
        catalogBO.addField(name);
        catalogBO.addField(products);

        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(employee);
        model.addBusinessObject(person);
        model.addBusinessObject(addressBO);
        model.addBusinessObject(countryBO);
        model.addBusinessObject(productBO);
        model.addBusinessObject(catalogBO);
        return model;
    }

}
