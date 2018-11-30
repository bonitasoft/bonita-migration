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
class EnsureAddedIndexOnArchProcessCommentIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context


    def setup() {
        migrationContext.setVersion("7.1.5")
    }

    def cleanup() {
        dbUnitHelper.dropTables(["arch_process_comment"] as String[])
    }

    @Unroll
    def "should #version platform modify index on arch_process_comment table"(String version) {
        setup:
        dbUnitHelper.createTables("$version/archProcessComment", "ArchProcessComment")

        when:
        new MigrateArchProcessCommentIndex().execute(migrationContext)

        then:
        def indexDefinition = dbUnitHelper.getIndexDefinition("arch_process_comment", "idx1_arch_process_comment")
        println("indexDefinition after migration:")
        println(indexDefinition.toString())

        indexDefinition.tableName == "arch_process_comment"
        indexDefinition.indexName == "idx1_arch_process_comment"

        def columnDefinitions = indexDefinition.getColumnDefinitions()
        columnDefinitions.size() == 2

        columnDefinitions.get(0).columnName.toLowerCase() == "sourceObjectId".toLowerCase()
        columnDefinitions.get(0).position == 1

        columnDefinitions.get(1).columnName.toLowerCase() == "tenantId".toLowerCase()
        columnDefinitions.get(1).position == 2

        where:
        version << ["6_4_2", "6_5_0"]

    }

}
