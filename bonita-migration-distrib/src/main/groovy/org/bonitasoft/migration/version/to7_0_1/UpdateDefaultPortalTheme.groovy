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

package org.bonitasoft.migration.version.to7_0_1

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
/**
 * @author Elias Ricken de Medeiros
 */
class UpdateDefaultPortalTheme extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        def currentTime = System.currentTimeMillis()
        // Portal
        def zip = this.getClass().getResourceAsStream("/version/to_7_0_1/bonita-portal-theme.zip")
        def css = this.getClass().getResourceAsStream("/version/to_7_0_1/bonita-portal-theme.zip")

        def type = "PORTAL"
        context.sql.executeUpdate("UPDATE theme SET lastUpdateDate = $currentTime WHERE isDefault = ${true} AND type = $type")
        context.sql.executeUpdate("UPDATE theme SET lastUpdateDate = ${currentTime + 1} WHERE isDefault = ${false} AND type = $type")
        context.sql.executeUpdate("UPDATE theme SET content = $zip.bytes, cssContent = $css.bytes WHERE isDefault = ${true} AND type = $type")
    }


    @Override
    String getDescription() {
        "update default portal theme"
    }
}
