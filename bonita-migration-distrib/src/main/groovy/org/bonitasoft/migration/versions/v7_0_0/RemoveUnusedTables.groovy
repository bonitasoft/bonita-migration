/**
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
 **/

package org.bonitasoft.migration.versions.v7_0_0

import groovy.sql.Sql
import org.bonitasoft.migration.core.DatabaseMigrationStep

/**
 * @author Baptiste Mesta
 */
class RemoveUnusedTables extends DatabaseMigrationStep{


    RemoveUnusedTables(Sql sql, String dbVendor) {
        super(sql, dbVendor)
    }

    @Override
    def migrate() {
        dropColumn("arch_process_instance", "migration_plan")
        dropColumn("process_instance", "migration_plan")
        dropColumn("process_definition", "migrationDate")
        sql.execute("DROP TABLE breakpoint")
        sql.execute("DELETE FROM sequence WHERE id = 10100")
        sql.execute("DROP TABLE arch_transition_instance")
        sql.execute("DELETE FROM sequence WHERE id = 20012")
    }
}
