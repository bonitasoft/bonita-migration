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

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class AddCSRFCookieSecureIT extends Specification {

    @Shared
    Logger logger = Mock(Logger)

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.4.0")
        dbUnitHelper.createTables("7_4_0/configuration", "api")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["configuration"] as String[])
    }

    @Unroll
    def "should migrate #propertiesContent to #expectedMigratedContent in security-config.properties"(
            def propertiesContent, def expectedMigratedContent) {
        setup:
        byte[] data = (propertiesContent as String).bytes
        dbUnitHelper.context.sql.execute("INSERT INTO configuration (tenant_id, content_type, resource_name, resource_content) VALUES(0, 'PLATFORM_PORTAL', 'security-config.properties', ? ) ", [data])
        def migrationStep = new AddCSRFCookieSecure()

        when:
        migrationStep.execute(migrationContext)

        then:
        def count = dbUnitHelper.countConfigFileWithContent("security-config.properties", expectedMigratedContent)
        count == 1

        where:
        propertiesContent             | expectedMigratedContent
        "security.csrf.enabled false" | "security.csrf.enabled false"
        "security.csrf.enabled true"  | "security.csrf.enabled true"
        "dummy property true"         | "dummy property true\nsecurity.csrf.enabled false"
    }

}
