/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.update.core

import org.bonitasoft.update.version.to11_0_0.AddTemporaryContentLargeObjectCleanupTriggerPostgres

class UpdateTemporaryContentLoTriggerOnly implements UpdateAction {

    UpdateContext context
    Logger logger
    DisplayUtil displayUtil

    @Override
    void run(boolean isSp) {
        if (context.dbVendor != UpdateStep.DBVendor.POSTGRES) {
            logger.info("Option -t/--create-lo-trigger is only applicable to PostgreSQL. Current vendor: ${context.dbVendor}. Nothing to do.")
            return
        }
        logger.info("Trigger-only mode (-t): creating temporary_content LO cleanup trigger/function")
        new AddTemporaryContentLargeObjectCleanupTriggerPostgres().execute(context)
        logger.info("Trigger-only mode (-t): done")
    }

    @Override
    List<String> getBannerAndGlobalWarnings() {
        return [
            "Running in trigger-only mode (-t).",
            "This will create/replace the PostgreSQL trigger/function for temporary_content LO cleanup.",
            "No version migration steps will be executed."
        ]
    }
}
