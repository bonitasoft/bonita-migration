/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.migration.version.to7_1_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Baptiste Mesta
 */
class MigrateFormMapping extends MigrationStep {
    @Override
    def execute(MigrationContext context) {
        context.databaseHelper.executeScript("MigrateFormMapping","")

        context.sql.eachRow("SELECT * FROM form_mapping") { formMapping ->
            def pageMapping = context.sql.firstRow("SELECT * FROM page_mapping where id = $formMapping.page_mapping_id AND tenantId = $formMapping.page_mapping_tenant_id")
            String target = "NONE"
            if (pageMapping != null) {
                if (pageMapping.url != null && !pageMapping.url.isEmpty()) {
                    target = "URL"
                } else if (pageMapping.urladapter != null && !pageMapping.urladapter.isEmpty()) {
                    target = "LEGACY"
                } else {
                    target = "INTERNAL"
                }
            }
            context.sql.executeUpdate("UPDATE form_mapping SET target=$target WHERE tenantId = $formMapping.tenantId and id = $formMapping.id")
        }
    }

    @Override
    String getDescription() {
        return "Migrate form mappings to add a the undefined/none state"
    }
}
