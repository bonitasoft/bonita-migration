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
class ApplicationMessageBuilderTest extends Specification {

    def retriever = Mock(ApplicationRetriever)

    private ApplicationMessageBuilder builder = new ApplicationMessageBuilder(retriever)

    void "BuildMessage should return warning message with all invalid applications by tenant"() {
        //given
        def applicationT1Id12 = new Application(tenantId: 1, id: 12, token: "content", version: "1.0", displayName: "Content")
        def applicationT1Id13 = new Application(tenantId: 1, id: 13, token: "api", version: "1.0", displayName: "Api")

        def applicationT2Id5 = new Application(tenantId: 2, id: 5, token: "theme", version: "1.0", displayName: "Theme")

        def invalidApplications =
                [new TenantApplications(tenantId: 1, applications: [applicationT1Id12, applicationT1Id13]),
                 new TenantApplications(tenantId: 2, applications: [applicationT2Id5])
                ]

        retriever.retrieveInvalidApplications() >> invalidApplications


        when:
        def message = builder.buildMessage();

        then:
        StringBuilder stb = new StringBuilder();
        stb.append(MessageUtil.buildInvalidApplicationTokenHeader()).append(MessageUtil.buildTenantMessage(applicationT1Id12.tenantId))
                .append(MessageUtil.buildApplicationMessage(applicationT1Id12))
                .append(MessageUtil.buildApplicationMessage(applicationT1Id13))
                .append(MessageUtil.buildTenantMessage(applicationT2Id5.tenantId))
                .append(MessageUtil.buildApplicationMessage(applicationT2Id5))
        message == stb.toString()
    }

    void "testBuildMessage should return empty message when input is empty"() {
        //given
        retriever.retrieveInvalidApplications() >> []

        when:
            def message = builder.buildMessage()

        then:
            //then
            message == ""

    }

}
