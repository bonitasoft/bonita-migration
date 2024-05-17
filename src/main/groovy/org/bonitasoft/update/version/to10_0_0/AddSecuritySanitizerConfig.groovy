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

class AddSecuritySanitizerConfig extends UpdateStep {

    public static final SECURITY_CONF_FILE = "security-config.properties"

    @Override
    execute(UpdateContext context) {
        context.configurationHelper.appendToSpecificConfigurationFileIfPropertyIsMissing('PLATFORM_PORTAL', SECURITY_CONF_FILE,
                'security.sanitizer.enabled', 'true', ' ', 'Enable/disable the Sanitizer protection activation (true/false). This sanitizer protects against multiple attacks such as XSS, but may restrict the use of some character sequences.')
        context.configurationHelper.appendToSpecificConfigurationFileIfPropertyIsMissing('PLATFORM_PORTAL', SECURITY_CONF_FILE,
                'security.sanitizer.exclude', 'email,password,password_confirm', ' ', 'Name of the Attributes excluded from sanitizer protection (comma separated)')
    }

    @Override
    String getDescription() {
        return "Add the security sanitizer properties to file '${SECURITY_CONF_FILE}'"
    }

}