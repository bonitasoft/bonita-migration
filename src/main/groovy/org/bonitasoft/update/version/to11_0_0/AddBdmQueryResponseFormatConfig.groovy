/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to11_0_0


import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class AddBdmQueryResponseFormatConfig extends UpdateStep {

    public static final String COMMUNITY_CONF_FILE = "bonita-platform-community-custom.properties"

    public static final String BDM_SERIALIZATION_COMMENT = """Controls whether BDM JSON serialization follows standard REST shapes or legacy array-based formats.
# When enabled (true, default):
#   - Count queries return { "value": n } instead of [n]
#   - Single-entity queries return { ? } instead of [ { ? } ]
# When disabled (false): legacy array format is used for backward compatibility."""

    public static final String DEFAULT_BDM_SERIALIZATION_KEY = "bonita.runtime.business-data.serialization.standard-shape.enabled"

    public static final String DEFAULT_BDM_SERIALIZATION_VALUE = "false"

    public static final String DEFAULT_BDM_SERIALIZATION_ENTRY = "$DEFAULT_BDM_SERIALIZATION_KEY=$DEFAULT_BDM_SERIALIZATION_VALUE"

    @Override
    execute(UpdateContext context) {
        context.configurationHelper.noTenant.appendToSpecificConfigurationFileIfPropertyIsMissing('PLATFORM_ENGINE', COMMUNITY_CONF_FILE,
                DEFAULT_BDM_SERIALIZATION_KEY, DEFAULT_BDM_SERIALIZATION_VALUE, "=", BDM_SERIALIZATION_COMMENT)
    }

    @Override
    String getDescription() {
        return "Add the BDM custom query response format property to " + COMMUNITY_CONF_FILE
    }
}
