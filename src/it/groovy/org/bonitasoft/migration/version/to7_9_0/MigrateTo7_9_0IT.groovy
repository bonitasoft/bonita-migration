/*
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_9_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Emmanuel Duchastenier
 */
class MigrateTo7_9_0IT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    def setup() {
        migrationContext.setVersion("7.9.0")
        dbUnitHelper.createTables("7_9_0/command")
    }

    def cleanup() {
        dbUnitHelper.dropTables(['command'] as String[])
    }

    def "should not have blocking message when db vendor is not oracle or oracle is in the right version"() {
        when:
        def messages = new MigrateTo7_9_0().getPreMigrationBlockingMessages(migrationContext)
        then:
        //in test oracle is > 12.2
        messages.size() == 0
    }

}
