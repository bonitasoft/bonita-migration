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

package org.bonitasoft.migration.core

import spock.lang.Specification

/**
 * @author Elias Ricken de Medeiros
 */
class ReporterTest extends Specification {

    def "should have no messages at the beginning"(){
        def reporter = new Reporter()

        expect: reporter.warnings.isEmpty()
    }

    def "calling addWarning should add one element to the warning list"() {
        def reporter = new Reporter()

        when:
        reporter.addWarning("warning 1")
        reporter.addWarning("warning 2")

        then:
        reporter.getWarnings() == ["warning 1", "warning 2"]

    }

    def "getWarning header should return the header of warning messages when there is warnings"() {
        def reporter = new Reporter()

        when:
        reporter.addWarning("warning")

        then:
        reporter.warningHeader == """
***************************************************************************************
/!\\  The platform was successfully migrated. However, some points need your attention.
***************************************************************************************
"""
    }

    def "getWarning header should be empty when there is no warnings"() {
        def reporter = new Reporter()

        expect:
        reporter.warningHeader == ""
    }

}
