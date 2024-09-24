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
package org.bonitasoft.update.core.database.schema
/**
 * @author Laurent Leseigneur
 */
class ForeignKeyDefinition {

    /**
     * name of the table that owns foreign key
     */
    private final String tableName

    /**
     * name of the foreign key
     */
    private final String foreignKeyName

    def ForeignKeyDefinition(String tableName, String foreignKeyName) {
        this.foreignKeyName = foreignKeyName
        this.tableName = tableName
    }

    @Override
    public String toString() {
        "tableName:${tableName} foreignKeyName:${foreignKeyName} refTableBame:${refTableName}"
    }
}
