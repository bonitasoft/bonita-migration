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
package org.bonitasoft.update.version.to10_2_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

/**
 * Update step to remove connector thread pool properties from configuration file
 */
class RemoveConnectorThreadPoolProperties extends UpdateStep {

    static final String CONFIG_FILE_NAME = "bonita-tenant-community-custom.properties"

    @Override
    def execute(UpdateContext context) {
        context.configurationHelper.removePropertiesInConfigFiles(CONFIG_FILE_NAME, "bonita.tenant.connector.corePoolSize",
                "bonita.tenant.connector.maximumPoolSize",
                "bonita.tenant.connector.keepAliveTimeSeconds")
    }

    @Override
    String getDescription() {
        return "Remove connector thread pool properties"
    }
}
