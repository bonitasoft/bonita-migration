/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Laurent Leseigneur
 */
class WarnAboutCSRF extends MigrationStep {

    public static final String PROPERTY_SECURITY_CSRF_ENABLED = "security.csrf.enabled"

    public static final String WARN_MESSAGE_CSRF = """CSRF security is currently disabled on this migrated platform whereas it's now enabled by default on new installations.
As a result your platform may be vulnerable.
We recommend you enable CSRF security by setting the property 'security.csrf.enabled' to true
in 'setup/platform_conf/current/platform_portal/security-config.properties' file of Bonita Platform, using the platform setup tool.

Then make sure your resources manage the CSRF token.

For more information, go to Bonita documentation web site and search with keywords "CSRF security" or "setup tool"

"""
    public static final String QUERY_PLATFORM_SECURITY_CONFIG = """
                SELECT
                c.resource_content as content
                FROM
                configuration c
                WHERE
                c.tenant_id = 0
                AND c.content_type = 'PLATFORM_PORTAL'
                AND c.resource_name='security-config.properties'
                """
    Logger logger

    def warning

    @Override
    def execute(MigrationContext context) {
        this.logger = context.logger

        def csrfEnabled = false
        def row = context.sql.firstRow(QUERY_PLATFORM_SECURITY_CONFIG)
        if (row != null) {
            def securityConfigContent = context.databaseHelper.getBlobContentAsString(row.content)
            def properties = new Properties()
            properties.load(new StringReader(securityConfigContent))
            if (properties.containsKey(PROPERTY_SECURITY_CSRF_ENABLED)) {
                csrfEnabled = ("true" == properties.getProperty(PROPERTY_SECURITY_CSRF_ENABLED))
            }
        }
        if (!csrfEnabled) {
            warning = WARN_MESSAGE_CSRF
        }
    }


    @Override
    String getDescription() {
        return "Check if CSRF is enabled on platform"
    }

    @Override
    String getWarning() {
        warning
    }
}
