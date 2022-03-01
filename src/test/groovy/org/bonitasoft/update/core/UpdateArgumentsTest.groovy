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
