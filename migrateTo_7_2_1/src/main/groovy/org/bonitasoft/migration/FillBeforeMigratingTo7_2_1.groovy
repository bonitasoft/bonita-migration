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
import org.bonitasoft.engine.api.PlatformAPIAccessor
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping
import org.bonitasoft.engine.bpm.contract.Type
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
import org.bonitasoft.engine.test.TestEngineImpl
import org.bonitasoft.migration.filler.FillAction
import org.bonitasoft.migration.filler.FillerInitializer
import org.bonitasoft.migration.filler.FillerShutdown
import org.bonitasoft.migration.filler.FillerUtils

/**
 * @author Baptiste Mesta
 */
class FillBeforeMigratingTo7_2_1 {

    /**
     * init platform before fill actions
     */
    @FillerInitializer
    public void init() {
        FillerUtils.initializeEngineSystemProperties()
        TestEngineImpl.instance.start()
    }

    /**
     * stop platform after all fill actions
     */
    @FillerShutdown
    public void shutdown() {
        //to be replaced by BonitaEngineRule with keepPlatformOnShutdown option
        def session = PlatformAPIAccessor.getPlatformLoginAPI().login("platformAdmin", "platform")
        PlatformAPIAccessor.getPlatformAPI(session).stopNode()
        PlatformAPIAccessor.getPlatformLoginAPI().logout(session)
    }

    @FillAction
    public void addProcessWithContractInput() {
        def client = new APIClient()
        client.login("install", "install")
        def user = client.identityAPI.createUser("userForContractInput", "bpm")
        client.logout()
        client.login("userForContractInput", "bpm")

        def builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithContractInput", "7.2.0")
        builder.addAutomaticTask("auto1")
        builder.addAutomaticTask("auto2")
        builder.addAutomaticTask("auto3")
        builder.addAutomaticTask("auto4")
        builder.addUserTask("step1", "myActor").addContract().addInput("taskInput", Type.BOOLEAN, "task input")
        builder.addUserTask("step2", "myActor").addContract()
        builder.addTransition("step1", "step2")
        builder.addActor("myActor")
        def contract = builder.addContract()
        contract.addInput("processInput", Type.BOOLEAN, "process input")

        def mapping = new ActorMapping()
        def actor = new Actor("myActor")
        actor.addUser("userForContractInput")
        mapping.addActor(actor)
        def businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setActorMapping(mapping).setProcessDefinition(builder.done()).done()

        def processDefinition = client.processAPI.deploy(businessArchive)
        client.processAPI.enableProcess(processDefinition.id)

        client.processAPI.startProcessWithInputs(processDefinition.id, [processInput: true])
        def instances

        def timeout = System.currentTimeMillis() + 3000
        while ((instances = client.processAPI.getPendingHumanTaskInstances(user.id, 0, 10, ActivityInstanceCriterion.DEFAULT)).size() < 1 && System.currentTimeMillis() < timeout) {
            Thread.sleep(200)
            println "wait 200"
        }
        client.processAPI.assignUserTask(instances.get(0).id, user.id)
        client.processAPI.executeUserTask(instances.get(0).id, [taskInput: true])

    }
}
