/**
 * Copyright (C) 2023 Bonitasoft S.A.
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

class RemoveEnableWordSearchConfig extends UpdateStep {

    @Override
    execute(UpdateContext context) {

        context.with {
            def configurationRaw = configurationHelper.sql.firstRow("SELECT RESOURCE_CONTENT FROM configuration WHERE RESOURCE_NAME = 'bonita-platform-community-custom.properties'")
            if (configurationRaw != null) {
                def configurationRawString = it.databaseHelper.getBlobContentAsString(configurationRaw.getProperty("resource_content"))
                String configurationNew = configurationRawString.replaceAll('(?m)#*[ \\t]*bonita\\.platform\\.persistence\\.platform\\.enableWordSearch.*$', "#")
                        .replaceAll('(?m)#*[ \\t]*bonita\\.platform\\.persistence\\.tenant\\.enableWordSearch.*$', "#")
                configurationHelper.deleteConfigurationFile("bonita-platform-community-custom.properties", 0l, "PLATFORM_ENGINE")
                configurationHelper.insertConfigurationFile("bonita-platform-community-custom.properties", 0l, "PLATFORM_ENGINE", configurationNew.getBytes())
            } else {
                throw new IllegalStateException("No file bonita-platform-community-custom.properties found in the database")
            }
        }
    }

    @Override
    String getDescription() {
        return "Remove the enableWordSearch parameter from bonita-platform-community-custom.properties'"
    }

}
