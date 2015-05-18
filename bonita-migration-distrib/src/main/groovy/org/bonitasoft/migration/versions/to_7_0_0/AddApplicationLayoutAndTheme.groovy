/**
 * Copyright (C) 2015 BonitaSoft S.A.
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

package org.bonitasoft.migration.versions.to_7_0_0

import groovy.sql.Sql
import org.bonitasoft.migration.core.DatabaseMigrationStep
import org.bonitasoft.migration.core.MigrationUtil

/**
 * @author Elias Ricken de Medeiros
 */
class AddApplicationLayoutAndTheme extends DatabaseMigrationStep {

    AddApplicationLayoutAndTheme(final Sql sql, final String dbVendor) {
        super(sql, dbVendor)
    }

    @Override
    def migrate() {
        addColumnWithForeignKey("layoutId")
        addColumnWithForeignKey("themeId")
        insertDefaultLayoutAndTheme()
    }

    private List<Object> insertDefaultLayoutAndTheme() {
        MigrationUtil.getTenantsId(dbVendor, sql).each { tenantId ->
            def layoutId = insertDefaultLayout(tenantId)
            def themeId = insertDefaultTheme(tenantId)
            setDefaultLayoutAndTheme(tenantId, layoutId, themeId)
        }
    }

    private void setDefaultLayoutAndTheme(long tenantId, def layoutId, def themeId) {
        def updateAppLayoutQuery = """
                UPDATE business_app
                SET layoutId=$layoutId, themeId=$themeId
                WHERE tenantId=$tenantId
            """
        def updatedApps = executeUpdate(updateAppLayoutQuery)
        println "$updatedApps applications were updated with default layout and theme on tenant $tenantId"
    }

    private insertDefaultLayout(long tenantId) {
        return insertPage(tenantId, "custompage_layout", "Application layout page",
                "This is a layout page dedicated to new born living applications. It is created and editable using the UI designer.", System.currentTimeMillis(),
                "bonita-layout-page.zip", "layout")
    }

    private insertDefaultTheme(long tenantId) {
        return insertPage(tenantId, "custompage_theme", "Application theme",
                "Application theme based on bootstrap \"Default\" theme. (see https://bootswatch.com/default/)", System.currentTimeMillis(),
                "bonita-theme-page.zip", "theme")
    }

    private insertPage(long tenantId, String pageName, String pageDisplayName, String pageDescription, def installationDate, String pageContentName, String contentType) {
        def nextPageId = getAndUpdateNextSequenceId(10120, tenantId)
        def insertPageQuery = """
                INSERT INTO page
                (
                    tenantId,
                    id,
                    name,
                    displayName,
                    description,
                    installationDate,
                    installedBy,
                    provided,
                    lastModificationDate,
                    lastUpdatedBy,
                    contentName,
                    content,
                    contentType
                ) VALUES(
                    $tenantId,
                    $nextPageId,
                    '$pageName',
                    '$pageDisplayName',
                    '$pageDescription',
                    $installationDate,
                    -1,
                    true,
                    $installationDate,
                    -1,
                    '$pageContentName',
                    ?,
                    '$contentType'
                )"""
        sql.executeUpdate(adaptFor(insertPageQuery), [new byte[0]])
        println "page $pageName inserted on tenant $tenantId"
        return nextPageId
    }

    private void addColumnWithForeignKey(String columnName) {
        addColumn("business_app", columnName, "INT8", null, null)
        addForeignKey(columnName)
    }

    def addForeignKey(String columnName) {
        sql.execute("ALTER TABLE business_app ADD CONSTRAINT fk_app_$columnName FOREIGN KEY (tenantid, $columnName) REFERENCES page (tenantid, id)".toString())
    }
}
