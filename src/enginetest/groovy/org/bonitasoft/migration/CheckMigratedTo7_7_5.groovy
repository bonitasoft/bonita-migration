/**
 * Copyright (C) 2017-2018 Bonitasoft S.A.
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
package org.bonitasoft.migration

import groovy.sql.Sql
import org.assertj.core.api.JUnitSoftAssertions
import org.junit.Rule
import spock.lang.Specification

class CheckMigratedTo7_7_5 extends Specification {

    @Rule
    public After7_2_0Initializer initializer = new After7_2_0Initializer()

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions()

    def "should have deleted orphan contract data"() {
        expect:
        getContractDataNames().sort() ==  [
                "processAInput1",
                "processAInput1",
                "processAInput2",
                "processAInput2",
                "processATaskInput1",
                "processATaskInput1",
                "processATaskInput2",
                "processATaskInput2"
        ]
    }

    Sql getConnection() {
        def dburl = System.getProperty("db.url")
        def dbDriverClassName = System.getProperty("db.driverClass")
        def dbUser = System.getProperty("db.user")
        def dbPassword = System.getProperty("db.password")
        Sql.newInstance(dburl, dbUser, dbPassword, dbDriverClassName)
    }


    private List<String> getContractDataNames() {
        def sql = getConnection()
        sql.rows("select name from arch_contract_data").collect { row ->
            row.get("name") as String
        }
    }


}
