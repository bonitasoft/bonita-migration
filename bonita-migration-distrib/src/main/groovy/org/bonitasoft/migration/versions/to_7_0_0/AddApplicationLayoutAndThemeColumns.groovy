/**
 * Copyright (C) 2015 BonitaSoft S.A.
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

package org.bonitasoft.migration.versions.to_7_0_0

import groovy.sql.Sql
import org.bonitasoft.migration.core.DatabaseMigrationStep

/**
 * @author Elias Ricken de Medeiros
 */
class AddApplicationLayoutAndThemeColumns extends DatabaseMigrationStep{

    AddApplicationLayoutAndThemeColumns(final Sql sql, final String dbVendor) {
        super(sql, dbVendor)
    }

    @Override
    def migrate() {
        addColumnWithForeignKey("layoutId")
        addColumnWithForeignKey("themeId")
    }

    private void addColumnWithForeignKey(String columnName) {
        addColumn("business_app", columnName, "INT8", null, null)
        addForeignKey(columnName)
    }

    def addForeignKey(String columnName) {
        sql.execute("ALTER TABLE business_app ADD CONSTRAINT fk_app_$columnName FOREIGN KEY (tenantid, $columnName) REFERENCES page (tenantid, id)".toString())
    }
}
