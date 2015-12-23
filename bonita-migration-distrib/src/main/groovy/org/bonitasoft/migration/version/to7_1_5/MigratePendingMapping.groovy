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

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.ORACLE

/**
 * @author Laurent Leseigneur
 */
class MigratePendingMapping extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        def helper = context.databaseHelper
        def foreignKeyName = (context.dbVendor == ORACLE) ? "fk_pMap_flnId" : "fk_pending_mapping_flownode_instanceId"
        if (!helper.hasForeignKeyOnTable("pending_mapping", foreignKeyName)) {
            helper.executeScript("MigratePendingMapping", "pendingMapping")
        }
    }

    @Override
    String getDescription() {
        "add missing foreign key on table pending_mapping if needed"
    }

}
