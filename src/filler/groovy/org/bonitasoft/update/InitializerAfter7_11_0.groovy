/**
 * Copyright (C) 2018 BonitaSoft S.A.
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
import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter
import org.bonitasoft.engine.bdm.model.BusinessObject
import org.bonitasoft.engine.bdm.model.BusinessObjectModel
import org.bonitasoft.engine.bdm.model.field.FieldType
import org.bonitasoft.engine.bdm.model.field.SimpleField
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
import org.bonitasoft.engine.expression.ExpressionBuilder
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.bonitasoft.update.filler.FillAction
import org.bonitasoft.update.filler.FillerInitializer
import org.bonitasoft.update.filler.FillerUtils
import org.junit.Rule

class InitializerAfter7_11_0 {

    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create().keepPlatformOnShutdown()

    @FillerInitializer
    void init() {
        FillerUtils.initializeEngineSystemProperties()
    }

    @FillAction
    public void fillOneUserWithTechnicalUser() {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install");
        def identityAPI = TenantAPIAccessor.getIdentityAPI(session)
        identityAPI.createUser("john", "bpm")
        TenantAPIAccessor.getLoginAPI().logout(session)
    }

    @FillAction
    void 'create and install a BDM and a process with Business Data'() {
        def client = new APIClient()
        client.login("install", "install")
        client.tenantAdministrationAPI.pause()

        client.tenantAdministrationAPI.installBusinessDataModel(new BusinessObjectModelConverter().zip(buildCustomBOM()))
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


    static BusinessObjectModel buildCustomBOM() {
        return new BusinessObjectModel().with {
            modelVersion = "1.0"
            productVersion = "7.11.3"
            addBusinessObject(new BusinessObject().with {
                qualifiedName = "com.compagny.BO"
                addField(new SimpleField().with {
                    name = "name"
                    type = FieldType.TEXT
                    length = 10
                    return it
                })
                return it
            })
            return it
        }
    }
}

