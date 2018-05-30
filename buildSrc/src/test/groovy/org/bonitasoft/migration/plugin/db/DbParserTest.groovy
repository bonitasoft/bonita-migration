/**
 * Copyright (C) 2018 Bonitasoft S.A.
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

package org.bonitasoft.migration.plugin.db

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class DbParserTest  extends Specification {

    def "should parse #dbUrl"() {
        setup:
        DbParser dbParser = new DbParser()

        expect:
        dbParser.extractDbConnectionSettings(dbUrl) == dbConnectionSettings

        where:
        dbUrl                                                       | dbConnectionSettings
        'jdbc:mysql://localhost:3306/bonita?allowMultiQueries=true' | new DbParser.DbConnectionSettings(dbUrl:'jdbc:mysql://localhost:3306/bonita?allowMultiQueries=true', serverName:'localhost', portNumber:'3306', databaseName:'bonita', genericUrl:'jdbc:mysql://localhost:3306/')
        'jdbc:oracle:thin:@localhost:1521:xe'                       | new DbParser.DbConnectionSettings(dbUrl:'jdbc:oracle:thin:@localhost:1521:xe', serverName: 'localhost', portNumber: '1521', databaseName: 'xe', genericUrl: 'jdbc:oracle:thin:@localhost:1521:xe')
        'jdbc:postgresql://hostdb:3615/bonita'                      | new DbParser.DbConnectionSettings(dbUrl:'jdbc:postgresql://hostdb:3615/bonita', serverName:'hostdb', portNumber:'3615', databaseName:'bonita', genericUrl:'jdbc:postgresql://hostdb:3615/')
        'jdbc:sqlserver://myhost:1433;database=migration'           | new DbParser.DbConnectionSettings(dbUrl:'jdbc:sqlserver://myhost:1433;database=migration', serverName:'myhost', portNumber:'1433', databaseName:'migration', genericUrl:'jdbc:sqlserver://myhost:1433')
    }

}
