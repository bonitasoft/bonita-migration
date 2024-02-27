/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to10_0_0


import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class AddEnableDynamicCheckConfig extends UpdateStep {

    public static final COMMUNITY_CONF_FILE = "bonita-tenant-community-custom.properties"

    public static final ENABLE_DYNAMIC_CHECK_COMMENT = "## Set this value to false to disable any dynamic permissions totally"

    public static final DEFAULT_ENABLE_DYNAMIC_CHECK_ENTRY = "#bonita.runtime.authorization.dynamic-check.enabled=true"

    @Override
    execute(UpdateContext context) {

        addEnableDynamicCheckEntry(context, DEFAULT_ENABLE_DYNAMIC_CHECK_ENTRY)
    }

    protected void addEnableDynamicCheckEntry(UpdateContext context, String enableDynamicCheckEntry) {
        context.configurationHelper.appendToAllConfigurationFilesWithName(COMMUNITY_CONF_FILE
                , "\n#" +
                ENABLE_DYNAMIC_CHECK_COMMENT + "\n" +
                enableDynamicCheckEntry)
    }

    @Override
    String getDescription() {
        return "Add the enable dynamic REST authorization check property to " + COMMUNITY_CONF_FILE
    }

}
