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
class ApplicationPageMessageBuilderTest extends Specification {

    def retriever = Mock(ApplicationPageRetriever)

    private ApplicationPageMessageBuilder builder = new ApplicationPageMessageBuilder(retriever);

    def static lineSeparator = System.getProperty("line.separator")

    void "buildMessage should return warning message containing invalid app pages by tenant and app"() {
        //given
        def applicationPageT1App12Id70 = new ApplicationPage(id: 70, token: "THEME", page: new Page(name: "page1", displayName: "Page example 1"))
        def applicationPageT1App12Id71 = new ApplicationPage(id: 71, token: "CONTENT", page: new Page(name: "page2", displayName: "Page example 2"))
        def applicationPageT1App13Id72 = new ApplicationPage(id: 72, token: "API", page: new Page(name: "page3", displayName: "Page example 3"))

        def applicationT1Id12 = new Application(tenantId: 1, id: 12, token: "content", version: "1.0", displayName: "Content", applicationPages: [applicationPageT1App12Id70, applicationPageT1App12Id71])
        def applicationT1Id13 = new Application(tenantId: 1, id: 13, token: "app", version: "1.0", displayName: "My app", applicationPages: [applicationPageT1App13Id72])

        def applicationPageT2App5Id15 = new ApplicationPage(id: 15, token: "API", page: new Page(name: "page4", displayName: "Page example 4"))
        def applicationT2Id5 = new Application(tenantId: 2, id: 5, token: "first-app", version: "1.0", displayName: "First application", applicationPages: [applicationPageT2App5Id15])

        List<TenantApplications> invalidApplications =
                [new TenantApplications(tenantId: 1, applications: [applicationT1Id12, applicationT1Id13]),
                 new TenantApplications(tenantId: 2, applications: [applicationT2Id5])
                ]

        retriever.retrieveApplicationsWithInvalidPages() >> invalidApplications


        when:
            def message = builder.buildMessage();
            StringBuilder stb = new StringBuilder();
            stb.append(MessageUtil.buildInvalidApplicationPageTokenHeader()).append(MessageUtil.buildTenantMessage(applicationT1Id12.tenantId))
                    .append(MessageUtil.buildApplicationMessage(applicationT1Id12))
                    .append(MessageUtil.buildApplicationPageMessage(applicationPageT1App12Id70))
                    .append(MessageUtil.buildApplicationPageMessage(applicationPageT1App12Id71))
                    .append(lineSeparator)
                    .append(MessageUtil.buildApplicationMessage(applicationT1Id13))
                    .append(MessageUtil.buildApplicationPageMessage(applicationPageT1App13Id72))
                    .append(lineSeparator)
                    .append(MessageUtil.buildTenantMessage(applicationT2Id5.tenantId))
                    .append(MessageUtil.buildApplicationMessage(applicationT2Id5))
                    .append(MessageUtil.buildApplicationPageMessage(applicationPageT2App5Id15))
                    .append(lineSeparator)
        then:
            message == stb.toString()

    }

    void "buildMessage should return empty message when input is empty"() {
            //given
            retriever.retrieveApplicationsWithInvalidPages() >> []

            when:
            def message = builder.buildMessage();

            then:
            message == ""
    }

}
