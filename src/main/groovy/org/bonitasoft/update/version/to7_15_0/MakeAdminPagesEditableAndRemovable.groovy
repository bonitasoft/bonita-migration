package org.bonitasoft.update.version.to7_15_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class MakeAdminPagesEditableAndRemovable extends UpdateStep {

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
        return "Make admin pages editable & removable"
    }

    def makePageEditableAndRemovable(UpdateContext context, String pageName) {
        if (context.dbVendor == DBVendor.ORACLE) {
            context.sql.executeUpdate(
                    """UPDATE page
                        SET editable = 1, removable = 1
                        WHERE name = ${pageName}""")
        } else if (context.dbVendor == DBVendor.SQLSERVER) {
            context.sql.executeUpdate(
                    """UPDATE page
                        SET editable = 'true', removable = 'true'
                        WHERE name = ${pageName}""")
        } else {
            context.sql.executeUpdate(
                    """UPDATE page
                        SET editable = true, removable = true
                        WHERE name = ${pageName}""")
        }
    }
}
