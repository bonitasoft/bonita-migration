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

import groovy.json.JsonBuilder

/**
 * @author Laurent Leseigneur
 */
class IndexDefinition {

    private final String indexName
    private final String tableName

    private final List<ColumnDefinition> columnDefinitions

    def IndexDefinition(String tableName, String indexName) {
        this.tableName = tableName
        this.indexName = indexName
        this.columnDefinitions = new ArrayList<>()
    }

    def addColumn(ColumnDefinition columnDefinition) {
        this.columnDefinitions.add(columnDefinition)
    }

    List<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions
    }

    String getTableName() {
        return tableName
    }

    String getIndexName() {
        return indexName
    }

    @Override
    String toString() {
        JsonBuilder builder=new JsonBuilder(this)
        builder.toPrettyString()
    }
}
