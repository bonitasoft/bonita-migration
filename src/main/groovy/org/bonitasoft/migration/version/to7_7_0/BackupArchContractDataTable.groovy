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
package org.bonitasoft.migration.version.to7_7_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Emmanuel Duchastenier
 */
class BackupArchContractDataTable extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        // drop unnecessary constraints:
        context.databaseHelper.dropPrimaryKey("arch_contract_data");
        context.databaseHelper.dropUniqueKey("arch_contract_data","uc_acd_scope_name");
        //Sometimes in oracle index linked to primary key was not deleted so we add a check after
        if (DBVendor.ORACLE == context.dbVendor) {
            context.databaseHelper.dropIndexIfExists("arch_contract_data","pk_arch_contract_data")
        }

        if (DBVendor.ORACLE != context.dbVendor) {
            context.databaseHelper.dropIndexIfExists('arch_contract_data',"idx_acd_scope_name")
        }


        context.databaseHelper.executeScript("7.7.0", "backup_arch_contract_data", "backup")

    }

    @Override
    String getDescription() {
        return "Backup table 'arch_contract_data' into 'arch_contract_data_backup' and recreate empty table 'arch_contract_data'"
    }




}
