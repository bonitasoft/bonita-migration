/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.identity.UserUpdater
import org.bonitasoft.engine.resources.TenantResourceType
import org.bonitasoft.engine.service.TenantServiceSingleton
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory
import org.bonitasoft.engine.test.junit.BonitaEngineRule
import org.bonitasoft.migration.filler.FillerUtils
import org.junit.Rule
import spock.lang.Specification

/**
 * @author Baptiste Mesta
 */
class CheckMigratedTo7_3_0 extends Specification {

    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create().reuseExistingPlatform()


    def setupSpec() {
        FillerUtils.initializeEngineSystemProperties()
    }

    def cleanup() {
    }

    def "tenant resource service should work"() {
        setup:
        def client = new APIClient()
        client.login("install", "install")
        def tenantResourcesService = TenantServiceSingleton.instance.getTenantResourcesService()
        def transactionService = ServiceAccessorFactory.instance.createPlatformServiceAccessor().transactionService
        when:
        transactionService.begin()
        tenantResourcesService.add("myTenantResource", TenantResourceType.BDM, "theContent".getBytes())
        transactionService.complete()
        transactionService.begin()
        def tenantResource = tenantResourcesService.get(TenantResourceType.BDM, "myTenantResource")
        transactionService.complete()

        then:
        new String(tenantResource.content) == "theContent"
    }

    def "client BDM zip file must be put in database"() {
        setup:
        def client = new APIClient()
        client.login("install", "install")
        expect:
        client.tenantAdministrationAPI.getClientBDMZip() != null
        cleanup:
        client.logout()
    }

    def "user avatar should be migrated"() {
        setup:
        def client = new APIClient()
        client.login("install", "install")
        expect:
        def icon = client.identityAPI.getIcon(client.identityAPI.getUserByUserName("userWithIcon").iconId)
        icon.mimeType == "image/png"
        icon.content == "the icon content".bytes
        cleanup:
        client.logout()
    }

    def "update user should create the icon"() {
        setup:
        def client = new APIClient()
        client.login("install", "install")
        def user = client.identityAPI.createUser("newUserForIconTest", "bpm")
        when:
        def updatedUser = client.identityAPI.updateUser(user.id, new UserUpdater().setIcon("theIcon.gif", "the gif content".bytes))
        then:
        def icon = client.identityAPI.getIcon(updatedUser.iconId)
        icon.mimeType == "image/gif"
        icon.content == "the gif content".bytes
        cleanup:
        client.logout()
    }

    def "should be able to execute migrated process with call activity"() {

        setup:
        def client = new APIClient()
        client.login("userForProcessWithCallActivity", "bpm")

        def processDefinitionId = client.processAPI.getProcessDefinitionId("ProcessWithCallActivity", "1.0")

        when:
        client.processAPI.startProcess(processDefinitionId)
        def HumanTaskInstance[] instances
        def timeout = System.currentTimeMillis() + 3000
        while ((instances = client.processAPI.getPendingHumanTaskInstances(client.session.userId, 0, 10, ActivityInstanceCriterion.DEFAULT)).size() < 2 && System.currentTimeMillis() < timeout) {
            Thread.sleep(200)
            println "wait 200"
        }
        then:
        instances.size() == 2
        client.processAPI.assignUserTask(instances[0].id, client.session.userId)
        client.processAPI.executeUserTask(instances[0].id, [taskInput: true])
        cleanup:
        client.logout()
    }


    def "custom page with previously no process definition id should be migrated with a 0 instead of null"() {
        given:
        def client = new APIClient()
        client.login("install", "install")
        when:
        def page = client.customPageAPI.getPageByName("custompage_APIBirthdayBonita")
        then:
        page != null
        client.customPageAPI.getPageContent(page.id).size() > 0
        page.processDefinitionId == null
    }
}