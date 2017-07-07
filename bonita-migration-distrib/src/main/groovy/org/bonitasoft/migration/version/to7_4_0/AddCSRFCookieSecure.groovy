/**
 * Copyright (C) 2017 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_4_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

public class AddCSRFCookieSecure extends MigrationStep {

    @Override
    public Object execute(MigrationContext context) {
        context.configurationHelper.appendToSpecificConfigurationFileIfPropertyIsMissing("PLATFORM_PORTAL"
                , "security-config.properties"
                , "security.csrf.cookie.secure"
                , "false"
                , " ", "Add or not the secure flag to the CSRF token cookie (HTTPS only)")
    }

    @Override
    public String getDescription() {
        return "Add CSRF cookie value to false in the security-config.properties file, if missing";
    }
}
