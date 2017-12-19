package org.bonitasoft.migration.core;

import spock.lang.Specification;

/**
 * @author Emmanuel Duchastenier
 */
class DisplayUtilTest extends Specification {

    DisplayUtil displayUtil

    def setup() {
        displayUtil = Spy(DisplayUtil)
    }

    def "should printInRectangleWithTitle draw about the longest message"() {
        when:
        displayUtil.printInRectangleWithTitle("My Title", ["my first message", "my Second message which is longer than the first one"] as String[])

        then:
        3 * displayUtil.printLine(54)
    }

    def "should printInRectangleWithTitle take title into account for longest message"() {
        when:
        displayUtil.printInRectangleWithTitle("My veeeeeeerrrrryyyyyyyy looooooooog Titleeeeeeee !!!!!", ["my first message", "my Second message"] as String[])

        then:
        3 * displayUtil.printLine(57)
    }

    def "should printInRectangleWithTitle split multiline messages when calculating size"() {
        when:
        displayUtil.printInRectangleWithTitle("a warning title", ["my first message\nis split over several lines for testing\npurposes", "my Second message"] as String[])

        then:
        3 * displayUtil.printLine(41)
    }
}