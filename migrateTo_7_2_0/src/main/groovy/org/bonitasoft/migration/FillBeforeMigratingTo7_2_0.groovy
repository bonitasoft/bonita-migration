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
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
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


    @FillAction
    public void deployProcessDefinitionXMLThatWillBeMigrated() {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install")

        def identityAPI = TenantAPIAccessor.getIdentityAPI(session)

        def user = identityAPI.createUser("userForMigratedProcess", "bpm")


        def builder = new ProcessDefinitionBuilder().createNewInstance("MyProcess to be migrated", "1.0-SNAPSHOT")
        builder.addAutomaticTask("step1")
        builder.addUserTask("step2","myActor")
        builder.addTransition("step1","step2")
        builder.addActor("myActor")
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
                .done()

        def processAPI = TenantAPIAccessor.getProcessAPI(session)
        def processDefinition = processAPI.deploy(businessArchive)




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
