/**
 * Copyright (C) 2015-2024 Bonitasoft S.A.
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

import groovy.json.JsonBuilder

/**
 * @author Laurent Leseigneur
 * @author Emmanuel Duchastenier
 */
class IndexDefinition {

    final String tableName
    final String indexName
    final boolean unique

    final List<String> columnNames

    IndexDefinition(String tableName, String indexName) {
        this.tableName = tableName
        this.indexName = indexName
        this.unique = false
        this.columnNames = new ArrayList<>()
    }

    IndexDefinition(String tableName, String indexName, boolean unique = false, String... columnNames) {
        this.tableName = tableName
        this.indexName = indexName
        this.unique = unique
        this.columnNames = new ArrayList<>()
        this.columnNames.addAll(columnNames)
    }

    def addColumn(String columnName) {
        columnNames.add(columnName)
    }

    String getTableName() {
        return tableName
    }

    String getIndexName() {
        return indexName
    }

    @Override
    String toString() {
        JsonBuilder builder = new JsonBuilder(this)
        builder.toPrettyString()
    }

    boolean isSameWithDifferentIndexName(IndexDefinition indexDef) {
        return (tableName == indexDef.tableName) && (indexName != indexDef.indexName) && columnNames == indexDef.columnNames
    }

    @Override
    boolean equals(Object o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        IndexDefinition that = (IndexDefinition) o
        if (indexName != that.indexName) return false
        if (tableName != that.tableName) return false
        return columnNames == that.columnNames
    }
}
