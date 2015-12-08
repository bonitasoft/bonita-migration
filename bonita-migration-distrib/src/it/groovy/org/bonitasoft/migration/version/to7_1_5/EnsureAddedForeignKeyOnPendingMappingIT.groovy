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
package org.bonitasoft.migration.version.to7_1_5
import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
/**
 * @author Laurent Leseigneur
 */
class EnsureAddedForeignKeyOnPendingMappingIT extends Specification {

    @Shared
    Logger logger = Mock(Logger)

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def setup() {
        migrationContext.setVersion("7.1.5")
    }

    def cleanup() {
        dbUnitHelper.dropTables(["pending_mapping","flownode_instance" ] as String[])
    }

    @Unroll
    def "should #version platform add foreign key on pending_mapping table"(String version) {
        setup:
        dbUnitHelper.createTables("$version/pendingMapping", "PendingMapping")

        when:
        new MigratePendingMapping().execute(migrationContext)

        then:
        dbUnitHelper.hasForeignKeyOnTable("pending_mapping","fk_pending_mapping_flownode_instanceId")==true

        where:
        version << ["6_4_2", "6_5_0"]

    }

}
