/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.update

import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.business.application.Application
import org.bonitasoft.engine.business.application.ApplicationMenu
import org.bonitasoft.engine.business.application.ApplicationMenuSearchDescriptor
import org.bonitasoft.engine.business.application.ApplicationPage
import org.bonitasoft.engine.business.application.ApplicationPageSearchDescriptor
import org.bonitasoft.engine.business.application.ApplicationSearchDescriptor
import org.bonitasoft.engine.identity.GroupCriterion
import org.bonitasoft.engine.identity.RoleCriterion
import org.bonitasoft.engine.identity.UserCriterion
import org.bonitasoft.engine.identity.UserMembershipCriterion
import org.bonitasoft.engine.profile.Profile
import org.bonitasoft.engine.search.Order
import org.bonitasoft.engine.search.SearchOptions
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.search.SearchResult
import org.junit.Rule
import spock.lang.Specification

class CheckUpdatedTo10_3_0 extends Specification {

    @Rule
    public After7_2_0Initializer initializer = new After7_2_0Initializer()

    def 'should be able to start a complex process after removing tenantId column from all tables'() {
        given:
        def client = new APIClient()
        client.login("walter.bates", "bpm")
        def processAPI = client.processAPI
        def processDefinitionId = processAPI.getProcessDefinitionId("executeConnectorOnFinishOfAnAutomaticActivityWithDataAsOutput", "1.0")

        when:
        processAPI.startProcess(processDefinitionId)
        final List<Profile> profiles = client.profileAPI.searchProfiles(new SearchOptionsBuilder(0, 1).done())
                .getResult()
        final SearchOptions searchOptionsBuilder = new SearchOptionsBuilder(0, 1)
                .filter(ApplicationSearchDescriptor.PROFILE_ID, profiles.get(0).getId())
                .sort(ApplicationSearchDescriptor.DISPLAY_NAME, Order.ASC).done()
        final SearchResult<Application> applications = client.applicationAPI.searchApplications(searchOptionsBuilder)
        def application = applications.getResult().get(0)
        final SearchResult<ApplicationPage> firstPage = client.applicationAPI
                .searchApplicationPages(new SearchOptionsBuilder(0, 1).filter(ApplicationPageSearchDescriptor.APPLICATION_ID, application.id).done())
        final SearchResult<ApplicationMenu> firstMenu = client.applicationAPI
                .searchApplicationMenus(new SearchOptionsBuilder(0, 1).filter(ApplicationMenuSearchDescriptor.APPLICATION_ID, application.id).done())

        then:
        application.displayName == "HR dashboard"
        firstPage.result[0].token == "home_page_token"
        firstMenu.result[0].displayName == "Home Menu"
    }

    def 'should be able to retrieve identity elements'() {
        given:
        def client = new APIClient()
        client.login("walter.bates", "bpm")

        when:
        def users = client.identityAPI.getUsers(0, 10, UserCriterion.USER_NAME_ASC)
        def groups = client.identityAPI.getGroups(0, 10, GroupCriterion.NAME_ASC)
        def roles = client.identityAPI.getRoles(0, 10, RoleCriterion.NAME_ASC)
        def walterBates = client.identityAPI.getUserByUserName("walter.bates")
        def userMemberships = client.identityAPI.getUserMemberships(
                walterBates.id, 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
        def customUserInfoDefs = client.identityAPI.getCustomUserInfoDefinitions(0, 10)
        def customUserInfo = client.identityAPI.getCustomUserInfo(walterBates.id, 0, 10)

        then:
        !users.isEmpty()
        users.collect { it.userName }.containsAll(["helen.kelly", "walter.bates"])
        !groups.isEmpty()
        groups.collect { it.name }.contains("acme")
        !roles.isEmpty()
        roles.collect { it.name }.contains("member")
        !userMemberships.isEmpty()
        userMemberships.collect { it.groupName }.contains("acme")
        userMemberships.collect { it.roleName }.contains("member")
        !customUserInfoDefs.isEmpty()
        customUserInfoDefs.collect { it.name }.contains("Skype ID")
        !customUserInfo.isEmpty()
        customUserInfo.collect { it.value }.contains("live:walter.bates")
    }
}
