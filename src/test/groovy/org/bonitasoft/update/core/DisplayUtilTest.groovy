/**
 * Copyright (C) 2017-2019 Bonitasoft S.A.
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

import spock.lang.Specification

import static org.assertj.core.api.Assertions.assertThat

/**
 * @author Emmanuel Duchastenier
 */
class DisplayUtilTest extends Specification {

    DisplayUtil displayUtil

    private def infos = []
    private def warnings = []
    private Logger logger = Stub()

    def setup() {
        displayUtil = new DisplayUtil()
        displayUtil.logger = logger

        logger.info(_) >> { args -> infos << args[0]; return }
        logger.warn(_) >> { args -> warnings << args[0]; return }
    }

    def cleanup() {
        infos = []
        warnings = []
    }

    def 'should log warnings in rectangle wth title draw about the longest message'() {
        when:
        displayUtil.logWarningsInRectangleWithTitle("My Title", ["my first message", "my Second message which is longer than the first one"] as String[])

        then:
        assertThat(warnings).containsExactly('+------------------------------------------------------+'
                , '|My Title                                              |'
                , '+------------------------------------------------------+'
                , '|my first message                                      |'
                , '|my Second message which is longer than the first one  |'
                , '+------------------------------------------------------+'
                )
    }

    def 'should log warnings in rectangle wth title take title into account for longest message'() {
        when:
        displayUtil.logWarningsInRectangleWithTitle("My veeeeeeerrrrryyyyyyyy looooooooog Titleeeeeeee !!!!!", ["my first message", "my Second message"] as String[])

        then:
        assertThat(warnings).containsExactly('+---------------------------------------------------------+'
                , '|My veeeeeeerrrrryyyyyyyy looooooooog Titleeeeeeee !!!!!  |'
                , '+---------------------------------------------------------+'
                , '|my first message                                         |'
                , '|my Second message                                        |'
                , '+---------------------------------------------------------+'
                )
    }

    def 'should log warnings in rectangle wth title split multiline messages when calculating size'() {
        when:
        displayUtil.logWarningsInRectangleWithTitle("a warning title", ["my first message\nis split over several lines for testing\npurposes", "my Second message"] as String[])

        then:
        assertThat(warnings).containsExactly('+-----------------------------------------+'
                , '|a warning title                          |'
                , '+-----------------------------------------+'
                , '|my first message                         |'
                , '|is split over several lines for testing  |'
                , '|purposes                                 |'
                , '|my Second message                        |'
                , '+-----------------------------------------+'
                )
    }

    def 'should generate a dash line'() {
        when:
        def line = displayUtil.dashLine(5)

        then:
        line == '+-----+'
    }

    def 'should generate a dash line when size is lower than zero'() {
        when:
        def line = displayUtil.dashLine(-3)

        then:
        line == '++'
    }

    def 'should generate spaces line'() {
        when:
        def line = displayUtil.spaces(7)

        then:
        line == '       '
    }

    def 'should generate spaces line when size is lower than zero'() {
        when:
        def line = displayUtil.spaces(-1)

        then:
        line == ''
    }

    def 'should generate a centered line in rectangle'() {
        when:
        def centeredLine = displayUtil.centeredLineInRectangle('hello', 12)

        then:
        centeredLine == '|   hello    |'
    }

    def 'should generate a centered line in rectangle when no additional spaces are required'() {
        when:
        def centeredLine = displayUtil.centeredLineInRectangle('hello boy', 9)

        then:
        centeredLine == '|hello boy|'
    }

    def 'should log info in centered rectangle'() {
        when:
        displayUtil.logInfoCenteredInRectangle('hello', 'how are you?', 'a very very very long long long line!')

        then:
        assertThat(infos).containsExactly('+---------------------------------------+'
                , '|                 hello                 |'
                , '|             how are you?              |'
                , '| a very very very long long long line! |'
                , '+---------------------------------------+'
                )
    }
}
