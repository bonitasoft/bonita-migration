/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to9_0_0

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Anthony Birembaut
 */
class UpdateTo9_0_0Test extends Specification {

    @Unroll
    def "should update to 9.0.0 include step '#stepName'"(def stepName) {
        given:
        def updateTo = new UpdateTo9_0_0()

        expect:
        def steps = updateTo.updateSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
            "CreateTemporaryContentTable",
            "AddAppVersionToPlatform",
            "AddMaintenanceMessageToPlatform",
            "RemoveUnusedSequences",
            "RemoveTenantIdFromProcessInstance",
            "RenameTechUserProperties"
        ]
    }
}
