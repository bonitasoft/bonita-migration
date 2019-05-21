/**
 * Copyright (C) 2018-2019 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_7_5

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

import groovy.transform.PackageScope

class DeleteOrphanArchContractData extends MigrationStep {

    // keep this static as new step instances are created in each VersionMigration
    private static boolean alreadyPerformed = false

    @Override
    def execute(MigrationContext context) {
        if (Boolean.getBoolean('bonita.migration.step.skip.775.delete.orphean.arch_contract_data')) {
            context.logger.info('Skipping step as per configuration')
            return
        }

        if (alreadyPerformed) {
            context.logger.info('Skipping step as it has already been performed')
            return null
        }
        performDeletion(context)
        alreadyPerformed = true
        return null
    }

    // visible for testing
    @PackageScope
    void performDeletion(MigrationContext context) {
        //7.7.5 version hard coded because step is launched in 7.7.0 and 7.7.5 but the scripts are in the 7.7.5 folder
        context.databaseHelper.executeScript("7.7.5", "remove_orphan_contract_data", "")
    }

    @Override
    String getDescription() {
        return "Delete archived contract data that are orphan (they were not deleted)"
    }
}
