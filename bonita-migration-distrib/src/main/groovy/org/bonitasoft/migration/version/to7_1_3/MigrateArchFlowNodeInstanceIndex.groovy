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
package org.bonitasoft.migration.version.to7_1_3

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Emmanuel Duchastenier
 */
class MigrateArchFlowNodeInstanceIndex extends MigrationStep {

    public static final String ARCH_FLOWNODE_INSTANCE = "arch_flownode_instance"

    @Override
    def execute(MigrationContext context) {
        def helper = context.databaseHelper
        helper.addOrReplaceIndex(ARCH_FLOWNODE_INSTANCE, "idx_afi_kind_lg3", "tenantId", "kind", "logicalGroup3")
    }


    @Override
    String getDescription() {
        return "Creates a new index on " + ARCH_FLOWNODE_INSTANCE + " table on columns (tenantId, kind, logicalGroup3)"
    }

}
