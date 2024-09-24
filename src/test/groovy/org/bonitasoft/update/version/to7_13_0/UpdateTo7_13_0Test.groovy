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
package org.bonitasoft.update.version.to7_13_0


import spock.lang.Specification
import spock.lang.Unroll
/**
 * @author Emmanuel Duchastenier
 */
class UpdateTo7_13_0Test extends Specification {

    @Unroll
    def "should update to 7.13.0 include step '#stepName'"(def stepName) {
        given:
        def updateTo = new UpdateTo7_13_0()

        expect:
        def steps = updateTo.updateSteps
        steps.collect {
            it.class.getSimpleName()
        }.contains(stepName)

        where:
        stepName << [
            "RemoveUselessV6formsConfiguration",
            "UpdateApplicationSchema",
            "UpdatePageSchema",
            "CreateNewPages",
            "UpdateProfileMenuToApplications",
            "RemoveThemes"
        ]
    }

    def "should 7.13.0 preUpdateWarnings warn about Java 11"() {
        setup:
        def UpdateTo7_13_0 = new UpdateTo7_13_0()

        when:
        def warnings = UpdateTo7_13_0.getPreUpdateWarnings(null)

        then:
        warnings.size() > 0
        warnings.any {
            it.contains("Java 11")
        }
    }

    def "should not display pre update warning regarding custom profiles before starting update'"() {
        given:
        def updateTo = new UpdateTo7_13_0()
        expect:
        !updateTo.getPreUpdateWarnings(null).any {
            it.contains("Custom profiles")
        }
    }
}
