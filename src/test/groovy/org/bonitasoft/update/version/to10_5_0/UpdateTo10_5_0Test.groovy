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
package org.bonitasoft.update.version.to10_5_0

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for UpdateTo10_5_0 migration orchestrator
 */
class UpdateTo10_5_0Test extends Specification {

    @Unroll
    def "should update to 10.5.0 include step '#stepName'"(def stepName) {
        given:
        def updateTo = new UpdateTo10_5_0()

        expect:
        def steps = updateTo.updateSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << ["RemoveEhcache2Configuration"]
    }

    def "should have pre-update warnings about Ehcache migration"() {
        given:
        def updateTo = new UpdateTo10_5_0()

        when:
        def warnings = updateTo.getPreUpdateWarnings(null)

        then:
        warnings != null
        warnings.length > 0
        warnings.any { it.contains("Ehcache") }
    }
}
