/**
 * Copyright (C) 2017 BonitaSoft S.A.
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

import groovy.sql.Sql
import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter
import org.bonitasoft.engine.bdm.model.BusinessObject
import org.bonitasoft.engine.bdm.model.BusinessObjectModel
import org.bonitasoft.engine.bdm.model.field.FieldType
import org.bonitasoft.engine.bdm.model.field.SimpleField
import org.bonitasoft.engine.bpm.contract.Type
import org.bonitasoft.engine.bpm.flownode.TimerType
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
import org.bonitasoft.engine.expression.Expression
import org.bonitasoft.engine.expression.ExpressionBuilder
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.bonitasoft.migration.filler.FillAction
import org.bonitasoft.migration.filler.FillerBdmInitializer
import org.bonitasoft.migration.filler.FillerInitializer
import org.bonitasoft.migration.filler.FillerUtils
import org.junit.Rule

import static org.assertj.core.api.Assertions.assertThat
import static org.awaitility.Awaitility.await

/**
 * @author Laurent Leseigneur
 */
class FillBeforeMigratingTo7_7_0 {

    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create().keepPlatformOnShutdown()

    @FillerInitializer
    void init() {
        FillerUtils.initializeEngineSystemProperties()
    }

    Sql getConnection() {
        def dburl = System.getProperty("db.url")
        def dbDriverClassName = System.getProperty("db.driverClass")
        def dbUser = System.getProperty("db.user")
        def dbPassword = System.getProperty("db.password")
        Sql.newInstance(dburl, dbUser, dbPassword, dbDriverClassName)
    }

    @FillAction
    void 'create elements with event triggers'() {
        //process has a timer, a throw signal and a throw message
        //also we send one signal and one message via api

        def process = new ProcessDefinitionBuilder()
                .createNewInstance("processWithEventTriggers", "1.0")
                .addIntermediateCatchEvent("waitTimer")
                .addTimerEventTriggerDefinition(TimerType.DURATION, new ExpressionBuilder().createConstantLongExpression(100_000_000))
                .getProcess()

        def client = new APIClient()
        client.login("install", "install")
        def processDefinition = client.processAPI.deployAndEnableProcess(process)
        def processInstance = client.processAPI.startProcess(processDefinition.id)
        client.processAPI.sendSignal("aSignalThrowAPI")
        client.processAPI.sendMessage("aMessageThrowAPI", expr("anOtherProcess"), expr("aFlowNode"), [:])

        //wait for the timer trigger to be registered
        await().until({
            client.processAPI
                    .searchTimerEventTriggerInstances(processInstance.id, new SearchOptionsBuilder(0, 100).done())
                    .count == 1
        })

        //we should have now in database:
        // 1 timer trigger
        // 1 message trigger
        // 1 signal trigger
        def sql = getConnection()
        await().until({
            def nbRows = sql.firstRow("select count(*) from event_trigger_instance")[0]
            println "number of rows in event_triger_instance: $nbRows"
            sql.eachRow("select * from event_trigger_instance") { row ->
                println row
            }
            nbRows == 3
        })

    }

    private Expression expr(String value) {
        new ExpressionBuilder().createConstantStringExpression(value)
    }

    @FillAction
    void 'create process with contract input'() {
        def process = new ProcessDefinitionBuilder().createNewInstance("ProcessWithContract", "1.0")
                .addContract().addInput("myInput", Type.TEXT, "a test input")
                .getProcess()

        def client = new APIClient()
        client.login("install", "install")

        def processDefinition = client.processAPI.deployAndEnableProcess(process)
        def processInstance = client.processAPI.startProcessWithInputs(processDefinition.id, [myInput: "theInputValue"])
        await().until({
            client.processAPI.getFinalArchivedProcessInstance(processInstance.id).endDate != null
        })
        assertThat(client.processAPI.getProcessInputValueAfterInitialization(processInstance.id, "myInput")).isEqualTo("theInputValue")
    }

    @FillerBdmInitializer
    void 'deploy BDM'() {
        def businessObjectModel = createBusinessObjectModel()
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter()
        final byte[] zip = converter.zip(businessObjectModel)

        def client = new APIClient()
        client.login("install", "install")
        client.tenantAdministrationAPI.pause()
        client.tenantAdministrationAPI.installBusinessDataModel(zip)
        client.tenantAdministrationAPI.resume()
        client.logout()
    }

    private static BusinessObjectModel createBusinessObjectModel() {
        final SimpleField firstName = new SimpleField()
        firstName.setName("name")
        firstName.setType(FieldType.STRING)
        firstName.setLength(Integer.valueOf(100))
        final BusinessObject testEntity = new BusinessObject()
        testEntity.setQualifiedName("com.company.model.TestEntity")
        testEntity.addField(firstName)
        final BusinessObjectModel model = new BusinessObjectModel()
        model.addBusinessObject(testEntity)
        model
    }

}

