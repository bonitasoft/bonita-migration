/**
 * Copyright (C) 2023 BonitaSoft S.A.
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
package org.bonitasoft.update

import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
import org.bonitasoft.engine.expression.ExpressionBuilder
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.bonitasoft.update.filler.FillAction
import org.junit.Rule

/**
 * @author Emmanuel Duchastenier
 */
class InitializerAfter8_0_0 extends CommonInitializer {

    // keepPlatformOnShutdown does not exist anymore since after 8.0.x:
    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create()

    @FillAction
    void 'create and install a BDM and a process with Business Data'() {
        def client = new APIClient()
        client.login("install", "install")
        client.tenantAdministrationAPI.pause()
        client.tenantAdministrationAPI.updateBusinessDataModel(new BusinessObjectModelConverter().zip(buildCustomBOM()))
        client.tenantAdministrationAPI.resume()

        def identityAPI = client.getIdentityAPI()
        def user = identityAPI.createUser("userToDeployAndStartProcess", "bpm")
        client.login("userToDeployAndStartProcess", "bpm")

        def processBuilder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithBusinessData", "11.0")
        processBuilder.setActorInitiator("myActorInitiator")
        processBuilder.addBusinessData("myBO", "com.compagny.BO", new ExpressionBuilder().createGroovyScriptExpression("createBusinessData",
                "new com.compagny.BO()", "com.compagny.BO"))
        def businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive()
        def actorInit = new Actor("myActorInitiator")
        actorInit.addUser(user.getUserName())
        def actorMapping = new ActorMapping()
        actorMapping.addActor(actorInit)
        def businessArchive = businessArchiveBuilder
                .setActorMapping(actorMapping)
                .setProcessDefinition(processBuilder.getProcess()).done()

        client.getProcessAPI().deployAndEnableProcess(businessArchive)
    }

}

