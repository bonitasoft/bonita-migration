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
class GlobalApplicationsMessageBuilderTest extends Specification {

    def firsRetriever = Mock(ApplicationRetriever)
    def secondRetriever = Mock(ApplicationRetriever)
    def tenantMessageBuilder = Mock(TenantApplicationsMessageBuilder)

    private GlobalApplicationsMessageBuilder builder = new GlobalApplicationsMessageBuilder([firsRetriever, secondRetriever], tenantMessageBuilder)

    void "buildMessage should include message for each retriever and tenant"() {
        //given
        def appT1 = new TenantApplications(tenantId: 1)
        def appT2 = new TenantApplications(tenantId: 2)
        def appT3 = new TenantApplications(tenantId: 3)
        def appT4 = new TenantApplications(tenantId: 4)

        firsRetriever.retrieveApplications() >> [appT1, appT3]
        firsRetriever.getHeader() >> "header1"

        secondRetriever.retrieveApplications() >> [appT2, appT4]
        secondRetriever.getHeader() >> "header2"

        tenantMessageBuilder.buildMessage(appT1) >> "appT1"
        tenantMessageBuilder.buildMessage(appT2) >> "appT2"
        tenantMessageBuilder.buildMessage(appT3) >> "appT3"
        tenantMessageBuilder.buildMessage(appT4) >> "appT4"

        when:
        def message = builder.buildMessage();

        then:
        StringBuilder stb = new StringBuilder()
        stb.append("header1").append("appT1").append("appT3").append("header2").append("appT2").append("appT4")
        message == stb.toString()
    }

    void "buildMessage should return empty message when input is empty"() {
        //given
        firsRetriever.retrieveApplications() >> []
        secondRetriever.retrieveApplications() >> []

        when:
        def message = builder.buildMessage()

        then:
        //then
        message == ""

    }

    void "buildMessage should contineu to evaluate follwoing retrievers when the current returns empty list"() {
        //given

        def tenantApplications = new TenantApplications(tenantId: 1)

        firsRetriever.retrieveApplications() >> []

        secondRetriever.retrieveApplications() >> [tenantApplications]
        secondRetriever.getHeader() >> "second header#"

        tenantMessageBuilder.buildMessage(tenantApplications) >> "appT1"

        when:
        def message = builder.buildMessage()

        then:
        //then
        message == "second header#appT1"

    }

}
