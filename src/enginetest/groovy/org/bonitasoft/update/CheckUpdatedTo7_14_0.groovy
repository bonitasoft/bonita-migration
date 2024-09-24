/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.update


import org.junit.Rule
import spock.lang.Specification

class CheckUpdatedTo7_14_0 extends Specification {

    @Rule
    public After7_2_0Initializer initializer = new After7_2_0Initializer()


    def "table report should not exist anymore"() {
        expect:
        !TestUtil.hasTable("report")
    }

    def "sequenceID for report should not exist anymore"() {
        expect:
        TestUtil.sql.firstRow("SELECT count(*) FROM sequence WHERE id = ${500}")[0] == 0
    }
}
