/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
 **/
package org.bonitasoft.update.version.to7_15_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class MakeAdminPagesFinal extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        makePageEditableAndRemovable(context,"custompage_adminCaseDetailsBonita")
        makePageEditableAndRemovable(context,"custompage_adminCaseListBonita")
        makePageEditableAndRemovable(context,"custompage_adminCaseVisuBonita")
        makePageEditableAndRemovable(context,"custompage_adminMonitoringBonita")
        makePageEditableAndRemovable(context,"custompage_adminProcessDetailsBonita")
        makePageEditableAndRemovable(context,"custompage_adminProcessListBonita")
        makePageEditableAndRemovable(context,"custompage_adminProcessVisuBonita")
        makePageEditableAndRemovable(context,"custompage_adminTaskDetailsBonita")
        makePageEditableAndRemovable(context,"custompage_adminTaskListBonita")
    }

    @Override
    String getDescription() {
        return "Make pages from Admin App not editable and not removable"
    }

    def makePageEditableAndRemovable(UpdateContext context, String pageName) {
        if (context.dbVendor == DBVendor.ORACLE) {
            context.sql.executeUpdate(
                    """UPDATE page
                        SET editable = 0, removable = 0
                        WHERE name = ${pageName}""")
        } else if (context.dbVendor == DBVendor.SQLSERVER) {
            context.sql.executeUpdate(
                    """UPDATE page
                        SET editable = 'false', removable = 'false'
                        WHERE name = ${pageName}""")
        } else {
            context.sql.executeUpdate(
                    """UPDATE page
                        SET editable = false, removable = false
                        WHERE name = ${pageName}""")
        }
    }
}
