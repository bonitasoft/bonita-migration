/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_13_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * Create new removable pages with empty content
 * The content will be automatically updated during the platform startup
 * Because provided === true
 *
 * @author Emmanuel Duchastenier
 * @author Dumitru Corini
 */
class CreateNewRemovablePages extends MigrationStep {

    @Override
    def execute(MigrationContext context) {

        final long currentTimeMillis = System.currentTimeMillis()

        context.with {
            databaseHelper.getAllTenants().each {
                tenant ->
                    def tenantId = tenant.id as long
                    insertPageIfMissing(it, currentTimeMillis, 'userCaseDetailsBonita.zip', 'custompage_userCaseDetailsBonita', tenantId)
                    insertPageIfMissing(it, currentTimeMillis, 'userCaseListBonita.zip', 'custompage_userCaseListBonita', tenantId)
            }
        }
    }

    @Override
    String getDescription() {
        return "Create new removable pages with empty content"
    }

    void insertPageIfMissing(MigrationContext migrationContext, long currentTimeMillis,
                             String zipName, String pageName, long tenantId) {
        migrationContext.with {
            if(sql.firstRow("SELECT count(id) FROM page WHERE tenantId = $tenantId AND name = $pageName")[0] > 0){
                logger.info("A page name $pageName already exists for tenant $tenantId, it will not be replaced by the one provided by the platform.")
                return
            }
            sql.executeInsert("""INSERT INTO page(tenantId , id, name, displayName, description, installationDate, 
installedBy, provided, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId, hidden)
VALUES (${tenantId}, ${databaseHelper.getAndUpdateNextSequenceId(10120L, tenantId)}, ${pageName}, 'will be updated', '', ${currentTimeMillis}, 
-1, ${true}, ${currentTimeMillis}, -1, ${zipName}, ${''.getBytes()}, '', 0, ${false})""")
        }
    }
}
