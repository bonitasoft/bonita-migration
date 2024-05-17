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

import groovy.sql.GroovyRowResult
import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class AddSecuritySanitizerConfigIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private AddSecuritySanitizerConfig updateStep = new AddSecuritySanitizerConfig()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("10_0_0")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["configuration"] as String[])
    }

    def "should add the sanitizer properties to existing security configuration file"() {
        given:
        byte[] originalContent = """#Enable/disable CSRF security filter
security.csrf.enabled true
#Add  or not the secure flag to the CSRF token cookie (HTTPS only)
security.csrf.cookie.secure false
#X-Frame-Options response header value
bonita.runtime.security.csrf.header.frame.options=SAMEORIGIN
#Content-Security-Policy response header value
bonita.runtime.security.csrf.header.content.security.policy=frame-ancestors 'self';""".bytes
        updateContext.sql.executeInsert("insert into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)",
                -1L, "PLATFORM_PORTAL", AddSecuritySanitizerConfig.SECURITY_CONF_FILE, originalContent)

        when:
        updateStep.execute(updateContext)
        List<GroovyRowResult> newConfRaw = updateContext.sql.rows("SELECT RESOURCE_CONTENT FROM configuration WHERE RESOURCE_NAME = '${AddSecuritySanitizerConfig.SECURITY_CONF_FILE}'")

        then:
        newConfRaw.size() == 1
        def confString = updateContext.databaseHelper.getBlobContentAsString(newConfRaw.get(0).getProperty("resource_content"))
        // Check existing lines are still there:
        confString.contains("security.csrf.enabled true\n")
        confString.contains("bonita.runtime.security.csrf.header.content.security.policy=frame-ancestors 'self';\n")
        // Check new lines are added correctly (and ENABLED by default):
        confString.contains("# Enable/disable the Sanitizer protection activation (true/false). This sanitizer protects against multiple attacks such as XSS, but may restrict the use of some character sequences.\n")
        confString.contains("security.sanitizer.enabled true\n")
        confString.contains("# Name of the Attributes excluded from sanitizer protection (comma separated)\n")
        confString.contains("security.sanitizer.exclude email,password,password_confirm")
    }
}
