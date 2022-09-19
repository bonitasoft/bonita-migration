package org.bonitasoft.update.version.to7_14_0

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
