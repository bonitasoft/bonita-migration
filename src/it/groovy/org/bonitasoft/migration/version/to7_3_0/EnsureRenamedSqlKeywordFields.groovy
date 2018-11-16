/*
 * Copyright (C) 2015 Bonitasoft S.A.
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
 */
package org.bonitasoft.migration.version.to7_3_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Guillaume Rosinosky
 */
class EnsureRenamedSqlKeywordFields extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    def setup() {
        migrationContext.setVersion("7.3.0")
        dbUnitHelper.createTables("7_3_0/queriableLog", "queriableLog")
    }

    def cleanup() {
        dbUnitHelper.dropTables(["queriable_log"] as String[])
    }

    def "should 7.3.0 platform fields renamed"() {
        when:
        new MigrateSqlReservedKeywords().execute(migrationContext)

        then:
        dbUnitHelper.hasColumnOnTable("queriable_log", "whatYear")
        dbUnitHelper.hasColumnOnTable("queriable_log", "whatMonth")
        dbUnitHelper.hasColumnOnTable("queriable_log", "log_timestamp")

        // there should be no error here
    }


}
