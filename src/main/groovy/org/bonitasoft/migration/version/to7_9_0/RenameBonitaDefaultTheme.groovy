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

    public static final String BONITA_THEME_NEW_NAME = 'custompage_themeBonita'
    public static final String BONITA_THEME_OLD_NAME = "custompage_bonitadefaulttheme"

    @Override
    def execute(MigrationContext context) {
        List<Long> listTenantId = getAllTenantIds(context)
        for (long tenantId : listTenantId) {
            if (themeWithNewNameAlreadyExists(context, tenantId)) {
                context.logger.error("""There is already a theme with name 'custompage_themeBonita' on tenant $tenantId. 
Bonita default theme will not be migrated to the new name on this tenant""")
            } else {
                updateNameAndDisplayNameAndContentNameOfPages(context, tenantId)
            }
        }
    }

    private static List<Long> getAllTenantIds(context) {
        return context.sql.rows("SELECT tenantId FROM page").tenantId
    }

    private static updateNameAndDisplayNameAndContentNameOfPages(MigrationContext context, long tenantId) {
        def newPageDisplayName = 'Bonita theme'
        def newPageContentName = 'bonita-theme.zip'
        context.databaseHelper.executeUpdate("UPDATE page SET NAME = '${BONITA_THEME_NEW_NAME}', displayName = '$newPageDisplayName', contentName = '$newPageContentName' WHERE NAME = '${BONITA_THEME_OLD_NAME}' AND tenantId = $tenantId")
    }

    private static boolean themeWithNewNameAlreadyExists(MigrationContext context, long tenantId) {
        return context.sql.rows("SELECT name FROM page WHERE name = 'custompage_themeBonita' AND tenantId = $tenantId").size() >= 1
    }

    @Override
    String getDescription() {
        return "Rename bonita default theme"
    }
}
