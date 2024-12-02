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
package org.bonitasoft.update.core

import com.github.zafarkhaja.semver.Version
import org.bonitasoft.update.version.to9_0_0.RemoveTenantIdFromIndexes

/**
 * Runner that reuses a specific UpdateStep to update the indexes only, in order
 * to remove the tenantId from them.
 */
class UpdateIndexesOnly implements UpdateAction {

    UpdateContext context
    Logger logger
    DisplayUtil displayUtil

    @Override
    void run(boolean isSp) {
        verifyMinimumVersion()

        def removeTenantIdFromIndexes = new RemoveTenantIdFromIndexes()
        removeTenantIdFromIndexes.execute(context)

        logger.info(removeTenantIdFromIndexes.description + ". Done")
    }

    @Override
    List<String> getBannerAndGlobalWarnings() {
        return [
            "The update tool was executed using '--update-indexes' option, it will update all database indexes.",
        ]
    }

    private void verifyMinimumVersion() {
        if (UpdateUtil.getPlatformVersion(context.sql) < Version.valueOf('9.0.0')) {
            logger.error("'--update-indexes' option is only supported for versions 9.0+")
            logger.error("Please note that updating your Bonita platform to a version 9.0+ will automatically update your indexes.")
            logger.error("There will be no need to use the '--update-indexes' option.")
            throw new IllegalStateException("'--update-indexes' option is only supported for versions 9.0+")
        }
    }
}
