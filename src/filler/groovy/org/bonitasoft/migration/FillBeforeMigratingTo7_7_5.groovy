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

import static org.assertj.core.api.Assertions.assertThat
import static org.awaitility.Awaitility.await

import groovy.sql.Sql
import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter
import org.bonitasoft.engine.bdm.model.BusinessObject
import org.bonitasoft.engine.bdm.model.BusinessObjectModel
import org.bonitasoft.engine.bdm.model.field.FieldType
import org.bonitasoft.engine.bdm.model.field.SimpleField
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping
import org.bonitasoft.engine.bpm.contract.Type
import org.bonitasoft.engine.bpm.process.ProcessDefinition
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

class FillBeforeMigratingTo7_7_5 {

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
    void 'create process with contracts'() {
        //process has a timer, a throw signal and a throw message
        //also we send one signal and one message via api

        def processA = new ProcessDefinitionBuilder()
                .createNewInstance("processA", "1.0").with { p ->
            p.addContract().with { c ->
                c.addInput("processAInput1", Type.TEXT, "an input")
                c.addInput("processAInput2", Type.TEXT, "an input")
            }
            p.addUserTask("task1", "actor").addContract().with { c ->
                c.addInput("processATaskInput1", Type.TEXT, "an input")
                c.addInput("processATaskInput2", Type.TEXT, "an input")
            }
            p.addActor("actor")
            p.process
        }
        def processB = new ProcessDefinitionBuilder()
                .createNewInstance("processB", "1.0").with { p ->
            p.addContract().with { c ->
                c.addInput("processBInput1", Type.TEXT, "an input")
                c.addInput("processBInput2", Type.TEXT, "an input")
            }
            p.addUserTask("task1", "actor").addContract().with { c ->
                c.addInput("processBTaskInput1", Type.TEXT, "an input")
                c.addInput("processBTaskInput2", Type.TEXT, "an input")
            }
            p.addActor("actor")
            p.process
        }

        def client = new APIClient()
        client.login("install", "install")
        def user = client.identityAPI.createUser("deleteArchContract", "bpm")
        ProcessDefinition processDefinitionA = deployProcessWithActor(client, processA)
        ProcessDefinition processDefinitionB = deployProcessWithActor(client, processB)
        def pa1 = client.processAPI.startProcessWithInputs(processDefinitionA.id, [processAInput1: "input1Value", processAInput2: "input2Value"])
        def pa2 = client.processAPI.startProcessWithInputs(processDefinitionA.id, [processAInput1: "input1Value", processAInput2: "input2Value"])
        def pb1 = client.processAPI.startProcessWithInputs(processDefinitionB.id, [processBInput1: "input1Value", processBInput2: "input2Value"])
        def pb2 = client.processAPI.startProcessWithInputs(processDefinitionB.id, [processBInput1: "input1Value", processBInput2: "input2Value"])

        //wait for all user tasks
        await().until({
            client.processAPI.getPendingHumanTaskInstances(user.id, 0, 100, null).size() == 4
        })

        client.processAPI.getPendingHumanTaskInstances(user.id, 0, 100, null).each { userTask ->
            client.processAPI.assignUserTask(userTask.id, user.id)
            if (userTask.rootContainerId == pa1.id || userTask.rootContainerId == pa2.id) {
                client.processAPI.executeUserTask(userTask.id, [processATaskInput1: "input1Value", processATaskInput2: "input2Value"])
            } else {
                client.processAPI.executeUserTask(userTask.id, [processBTaskInput1: "input1Value", processBTaskInput2: "input2Value"])
            }
        }

        await().until({
            client.processAPI.searchProcessInstances(new SearchOptionsBuilder(0, 100).done()).count == 0
        })

        assertThat(getNbRowInArchContractData()).isEqualTo(16)

        client.processAPI.deleteArchivedProcessInstancesInAllStates([pb1.id, pb2.id])

        //bug in the delete we should have 4
        assertThat(getNbRowInArchContractData()).isEqualTo(16)


    }

    private long getNbRowInArchContractData() {
        def sql = getConnection()
        def nbRows = sql.firstRow("select count(*) from arch_contract_data")[0] as long
        println "number of rows in arch_contract_data: $nbRows"
        sql.eachRow("select * from arch_contract_data") { row ->
            println row
        }
        nbRows
    }

    private ProcessDefinition deployProcessWithActor(APIClient client, processA) {
        def processDefinitionA = client.processAPI.deployAndEnableProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().with { b ->
                    b.setActorMapping(new ActorMapping().with { mapping ->
                        mapping.addActor(new Actor("actor").with { a ->
                            a.addUser("deleteArchContract")
                            a
                        })
                        mapping
                    })
                    b.setProcessDefinition(processA)
                    b.done()
                }
        )
        processDefinitionA
    }


}

