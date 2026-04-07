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
package org.bonitasoft.update.version.to11_0_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for UpdateTo11_0_0 migration orchestrator
 */
class UpdateTo11_0_0Test extends Specification {

    @Unroll
    def "should update to 11.0.0 include step '#stepName'"(def stepName) {
        given:
        def updateTo = new UpdateTo11_0_0()

        expect:
        def steps = updateTo.updateSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
            "RemoveEhcache2Configuration",
            "AddBdmQueryResponseFormatConfig",
            "AddTemporaryContentLargeObjectCleanupTriggerPostgres",
            "CreateDataRetentionConfigTable",
            "CreateDataRetentionBdmTrackingTable"
        ]
    }

    def "should 11.0.0 preUpdateWarnings warn about Ehcache migration"() {
        given:
        def updateTo = new UpdateTo11_0_0()

        when:
        def warnings = updateTo.getPreUpdateWarnings(null)

        then:
        warnings != null
        warnings.length > 0
        warnings.any { it.contains("Ehcache") }
    }

    def "should 11.0.0 preUpdateWarnings warn about BDM query response format"() {
        given:
        def version = new UpdateTo11_0_0()

        when:
        def warnings = version.getPreUpdateWarnings(null)

        then:
        warnings.size() > 0
        warnings.any { it.contains("BDM custom query response formats") }
        warnings.any { it.contains("bonita.runtime.business-data.serialization.standard-shape.enabled") }
        warnings.any { it.contains("documentation.bonitasoft.com") }
    }

    def "should 11.0.0 preUpdateWarnings include PostgreSQL LO cleanup warning only for PostgreSQL"() {
        given:
        def version = new UpdateTo11_0_0()
        def postgresContext = Mock(UpdateContext)
        postgresContext.dbVendor >> UpdateStep.DBVendor.POSTGRES
        def oracleContext = Mock(UpdateContext)
        oracleContext.dbVendor >> UpdateStep.DBVendor.ORACLE

        when:
        def pgWarnings = version.getPreUpdateWarnings(postgresContext)
        def oracleWarnings = version.getPreUpdateWarnings(oracleContext)

        then:
        pgWarnings.any { it.contains("PostgreSQL Large Objects cleanup") }
        !oracleWarnings.any { it.contains("PostgreSQL Large Objects cleanup") }
    }

    def "should 11.0.0 preUpdateWarnings not include PostgreSQL warning when context is null"() {
        given:
        def version = new UpdateTo11_0_0()

        when:
        def warnings = version.getPreUpdateWarnings(null)

        then:
        !warnings.any { it.contains("PostgreSQL Large Objects cleanup") }
    }
}
