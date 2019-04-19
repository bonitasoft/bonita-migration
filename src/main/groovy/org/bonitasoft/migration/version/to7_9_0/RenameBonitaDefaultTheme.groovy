/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_9_0

import groovy.sql.GroovyRowResult
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class RenameBonitaDefaultTheme extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        List<GroovyRowResult> listTenantId = getAllTenantIds(context);
        for(def index = 0; index < listTenantId.size(); index++) {
            if (themeNameAlreadyExists(context, listTenantId.get(index))) {
                context.logger.error('There is already a theme with name custompage_themeBonita. Bonita default theme will not be migrated to its new name')
            } else {
                updateNameAndDisplayNameAndContentNameOfPages(context, listTenantId.get(index))
            }
        }
    }

    private static List<GroovyRowResult> getAllTenantIds(context) {
        return context.sql.rows("SELECT tenantId FROM page")
    }

    private static updateNameAndDisplayNameAndContentNameOfPages(MigrationContext context, GroovyRowResult tenantIdRow) {
        def newPageName = 'custompage_themeBonita'
        def newPageDisplayName = 'Bonita theme'
        def newPageContentName = 'bonita-theme.zip'
        def tenantId = tenantIdRow.getAt(0)
        context.logger.info("Updating theme information for all profile entries")
        context.databaseHelper.executeUpdate("UPDATE page SET NAME = '$newPageName', displayName = '$newPageDisplayName', contentName = '$newPageContentName' WHERE NAME = 'custompage_bonitadefaulttheme' AND tenantId = $tenantId")
    }

    private static boolean themeNameAlreadyExists(MigrationContext context, GroovyRowResult tenantIdRow) {
        def tenantId = tenantIdRow.getAt(0)
        return context.sql.rows("SELECT name FROM page WHERE name = 'custompage_themeBonita' AND tenantId = $tenantId").size() >= 1
    }

    @Override
    String getDescription() {
        return "Rename bonita default theme"
    }
}
