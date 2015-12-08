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

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * author Laurent Leseigneur
 */
class MigrateArchProcessCommentIndex extends MigrationStep {

    public static final String ARCH_PROCESS_COMMENT = "arch_process_comment"

    @Override
    def execute(MigrationContext context) {
        def helper = context.databaseHelper
        def indexDefinition = helper.getIndexDefinition(ARCH_PROCESS_COMMENT, "idx1_arch_process_comment")

        if (!"sourceObjectId".equalsIgnoreCase(indexDefinition.columnDefinitions.get(0).columnName)) {
            helper.addOrReplaceIndex(ARCH_PROCESS_COMMENT, "idx1_arch_process_comment", "sourceobjectid", "tenantid")
        }
        else{
            println("index does not need to be migrated.")
        }

    }

    @Override
    String getDescription() {
        return "Fix index column order on table " + ARCH_PROCESS_COMMENT
    }

}
