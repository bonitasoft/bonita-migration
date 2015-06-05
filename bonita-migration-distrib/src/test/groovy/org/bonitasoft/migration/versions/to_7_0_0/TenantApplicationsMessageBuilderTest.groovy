/**
 * Copyright (C) 2015 BonitaSoft S.A.
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

package org.bonitasoft.migration.versions.to_7_0_0

import spock.lang.Specification

/**
 * @author Elias Ricken de Medeiros
 */
class TenantApplicationsMessageBuilderTest extends Specification {

    def static lineSeparator = System.getProperty("line.separator")

    private TenantApplicationsMessageBuilder builder = new TenantApplicationsMessageBuilder()

    def "buildMessageForTenant should include all tenant applications"() {
        //given
        def appPage1App1 = new ApplicationPage(id: 70, token: "THEME", page: new Page(name: "page1", displayName: "Page example 1"))
        def appPage2App1 = new ApplicationPage(id: 71, token: "CONTENT", page: new Page(name: "page2", displayName: "Page example 2"))
        def appPage1App2 = new ApplicationPage(id: 72, token: "API", page: new Page(name: "page3", displayName: "Page example 3"))

        def app1 = new Application(tenantId: 1, id: 12, token: "content", version: "1.0", displayName: "Content", applicationPages: [appPage1App1, appPage2App1])
        def app2 = new Application(tenantId: 1, id: 13, token: "api", version: "1.0", displayName: "Api", applicationPages: [appPage1App2])
        def app3 = new Application(tenantId: 1, id: 14, token: "app", version: "1.0", displayName: "My app")
        def app4 = new Application(tenantId: 1, id: 15, token: "rd", version: "1.0", displayName: "R&D")

        def applications = new TenantApplications(tenantId: 1, applications: [app1, app2, app3, app4])

        when:
        def message = builder.buildMessage(applications);

        then:
        StringBuilder stb = new StringBuilder();
        stb.append(MessageUtil.buildTenantMessage(app1.tenantId))
                .append(MessageUtil.buildApplicationMessage(app1))
                .append(MessageUtil.buildApplicationPageMessage(appPage1App1))
                .append(MessageUtil.buildApplicationPageMessage(appPage2App1))
                .append(lineSeparator)
                .append(MessageUtil.buildApplicationMessage(app2))
                .append(MessageUtil.buildApplicationPageMessage(appPage1App2))
                .append(lineSeparator)
                .append(MessageUtil.buildApplicationMessage(app3))
                .append(MessageUtil.buildApplicationMessage(app4))
        message == stb.toString()
    }

}
