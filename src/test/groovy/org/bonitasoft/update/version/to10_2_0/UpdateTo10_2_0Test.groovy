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
package org.bonitasoft.update.version.to10_2_0


import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep
import spock.lang.Specification
import spock.lang.Unroll

class UpdateTo10_2_0Test extends Specification {

    @Unroll
    def "should update to 10.2.0 include step '#stepName'"(def stepName) {
        given:
        def updateTo = new UpdateTo10_2_0()

        expect:
        def steps = updateTo.updateSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
            "AddColumnLinkToBusinessApp",
            "CreateRefBizDataInstIndex",
            "RemoveWorkThreadPoolProperties",
            "RemoveWorkDelayOnXAResourceProperties",
            "RemoveConnectorThreadPoolProperties"
        ]
    }

    def "should 10.2.0 pre-update block if non-postgres dbVendor detected"() {
        given:
        def updateTo = new UpdateTo10_2_0()
        def updateContext = Mock(UpdateContext)
        updateContext.dbVendor >> UpdateStep.DBVendor.MYSQL

        when:
        def warnings = updateTo.getPreUpdateBlockingMessages(updateContext)

        then:
        warnings.size() > 0
        UpdateTo10_2_0.BLOCK_IF_NON_POSTGRES_DATABASE.every {
            warnings.contains(it)
        }
    }

    def "should 10.2.0 pre-update not block if postgres dbVendor detected"() {
        given:
        def updateTo = new UpdateTo10_2_0()
        def updateContext = Mock(UpdateContext)
        updateContext.dbVendor >> UpdateStep.DBVendor.POSTGRES

        expect:
        updateTo.getPreUpdateBlockingMessages(updateContext).size() == 0
    }
}
