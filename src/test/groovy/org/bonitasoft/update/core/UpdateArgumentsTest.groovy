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
package org.bonitasoft.update.core

import org.apache.commons.cli.ParseException
import spock.lang.Specification
import spock.lang.Unroll

class UpdateArgumentsTest extends Specification {

    @Unroll
    def "should parse verify option"() {
        given:
        def arguments = ["--verify"] as String[]

        when:
        def parsedArguments = UpdateArguments.parse(arguments)

        then:
        parsedArguments.verify
    }

    def "should have verify not set when option is not present"() {
        given:
        def arguments = [] as String[]

        when:
        def parsedArguments = UpdateArguments.parse(arguments)

        then:
        !parsedArguments.verify
    }

    def "should fail when argument is unknown"() {
        given:
        def arguments = ["--toto"] as String[]


        when:
        UpdateArguments.parse(arguments)

        then:
        def exception = thrown(ParseException)
        exception.message == "Unrecognized option: --toto"
    }
}
