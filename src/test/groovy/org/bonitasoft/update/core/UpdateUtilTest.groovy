/**
 * Copyright (C) 2020 Bonitasoft S.A.
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

import com.github.zafarkhaja.semver.Version
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Emmanuel Duchastenier
 */
class UpdateUtilTest extends Specification {

    Sql sql = Mock(Sql)

    @Unroll
    def "getPlatformVersion(#versionInDatabase) should be parsed as #resultVersion"() {
        setup:
        sql.firstRow(_ as String) >> new GroovyRowResult(["key": "$versionInDatabase"])
        when:
        def interpretedVersion = UpdateUtil.getPlatformVersion(sql)
        then:
        interpretedVersion == Version.valueOf(resultVersion)
        where:
        versionInDatabase || resultVersion
        "7.11"            || "7.11.0"
        "7.10.4"          || "7.10.0"
        "7.12"            || "7.12.0"
        "8.0"             || "8.0.0"
    }

    @Unroll
    def "getDisplayVersion(#original) must return #result"() {
        when:
        def interpretedVersion = UpdateUtil.getDisplayVersion(original)
        then:
        interpretedVersion == result
        where:
        original          || result
        "7.11.0"          || "7.11"
        "7.10.4"          || "7.10"
        "7.12.0"          || "7.12"
        "8.0.0"           || "8.0"
    }
}
