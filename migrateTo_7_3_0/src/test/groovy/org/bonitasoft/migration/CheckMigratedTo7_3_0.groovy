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

import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.identity.UserUpdater
import org.bonitasoft.engine.resources.TenantResourceType
import org.bonitasoft.engine.service.TenantServiceSingleton
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory
import org.bonitasoft.engine.session.APISession
import org.bonitasoft.engine.test.TestEngine
import org.bonitasoft.engine.test.annotation.Engine
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

    @Engine
    public TestEngine testEngine

    def APISession session


    def setupSpec() {
        FillerUtils.initializeEngineSystemProperties()
    }

    def cleanup() {
        TenantAPIAccessor.loginAPI.logout(session)
    }

    def "tenant resource service should work"() {
        setup:
        session = TenantAPIAccessor.loginAPI.login("install", "install")
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
        session = TenantAPIAccessor.loginAPI.login("install", "install")
        expect:
        TenantAPIAccessor.getTenantAdministrationAPI(session).getClientBDMZip() != null
    }

    def "user avatar should be migrated"() {
        setup:
        session = TenantAPIAccessor.loginAPI.login("install", "install")
        def identityAPI = TenantAPIAccessor.getIdentityAPI(session)
        expect:
        def icon = identityAPI.getIcon(identityAPI.getUserByUserName("userWithIcon").iconId)
        icon.mimeType == "image/png"
        icon.content == "the icon content".bytes
    }

    def "update user should create the icon"() {
        setup:
        session = TenantAPIAccessor.loginAPI.login("install", "install")
        def identityAPI = TenantAPIAccessor.getIdentityAPI(session)
        def user = identityAPI.createUser("newUserForIconTest", "bpm")
        when:
        def updatedUser = identityAPI.updateUser(user.id, new UserUpdater().setIcon("theIcon.gif", "the gif content".bytes))
        then:
        def icon = identityAPI.getIcon(updatedUser.iconId)
        icon.mimeType == "image/gif"
        icon.content == "the gif content".bytes
    }
}