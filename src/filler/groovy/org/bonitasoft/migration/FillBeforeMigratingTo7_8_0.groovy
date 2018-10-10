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
package org.bonitasoft.migration

import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.bpm.bar.BarResource
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping
import org.bonitasoft.engine.bpm.form.FormMappingModelBuilder
import org.bonitasoft.engine.bpm.process.ConfigurationState
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder
import org.bonitasoft.engine.form.FormMappingTarget
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.bonitasoft.migration.filler.FillAction
import org.bonitasoft.migration.filler.FillerInitializer
import org.bonitasoft.migration.filler.FillerUtils
import org.bonitasoft.migration.test.TestUtil
import org.junit.Rule

import static org.assertj.core.api.Assertions.assertThat

/**
 * @author Danila Mazour
 * @author Emmanuel Duchastenier
 */
class FillBeforeMigratingTo7_8_0 {

    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create().keepPlatformOnShutdown()

    @FillerInitializer
    void init() {
        FillerUtils.initializeEngineSystemProperties()
    }

    @FillAction
    void 'insert page into the engine'() {
        def client = new APIClient()
        client.login("install", "install")
        def content = TestUtil.createTestPageContent("custompage_APIBirthdayBonita", "APIBirthdayBonita", "a custom page that should not be hidden after migration")
        def page = client.customPageAPI.createPage("mypage.zip", content)
        assertThat(page).isNotNull()
    }

    @FillAction
    void 'add processes with v6 case overview before migration'() {
        def client = new APIClient()
        client.login("install", "install")

        User user = client.identityAPI.createUser("userForv6FormMigrationProcess", "bpm")
        def actor = new Actor("myActorInitiator")
        actor.addUser(user.getUserName())
        def actorMapping = new ActorMapping()
        actorMapping.addActor(actor)

        ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithV6CaseOverview", "7.7.4")
        processBuilder.addUserTask("step1", "myActorInitiator")
        processBuilder.addActor("myActorInitiator")
        BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder()
                .createNewBusinessArchive()
                .setProcessDefinition(processBuilder.done())
                .setFormMappings(
                FormMappingModelBuilder.buildFormMappingModel()
                        .addProcessStartForm("processStartForm", FormMappingTarget.LEGACY)
                        .addTaskForm("task1Form", FormMappingTarget.LEGACY, "step1")
                        .addProcessOverviewForm("process1OverviewForm", FormMappingTarget.LEGACY).build())

        businessArchiveBuilder.setActorMapping(actorMapping)
        businessArchiveBuilder.addExternalResource(new BarResource('forms/forms.xml', 'someContent'.bytes))

        // do not enable it for migration to be possible:
        def processDefinition = client.processAPI.deploy(businessArchiveBuilder.done())
        def processDeploymentInfo = client.processAPI.getProcessDeploymentInfo(processDefinition.id)
        def resource = client.processAPI.getProcessResources(processDefinition.id, 'resources/forms/forms.xml')

        client.logout()

        assertThat(processDeploymentInfo.getConfigurationState()).isEqualTo(ConfigurationState.RESOLVED)
        assertThat(resource).isNotNull()
        assertThat(resource['resources/forms/forms.xml']).isEqualTo('someContent'.bytes)
    }

}

