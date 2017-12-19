/*
 * Copyright (C) 2016 Bonitasoft S.A.
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
 */
package org.bonitasoft.migration.version.to7_4_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Laurent Leseigneur
 */
class WarnAboutCSRFIT extends Specification {


    Logger logger = Mock(Logger)

    MigrationContext migrationContext = new MigrationContext(logger: logger)

    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def setup() {
        migrationContext.setVersion("7.4.0")
        migrationContext.setLogger(logger)
        dropTestTables()
        dbUnitHelper.createTables("7_4_0/csrf", "csrf")
    }

    def cleanup() {
        dropTestTables()
    }

    @Unroll
    def "should warn be #expectedWarning when #description"(
            def propertiesContent, def expectedWarning, def description) {
        setup:
        byte[] data = (propertiesContent as String).bytes
        dbUnitHelper.context.sql.execute("INSERT INTO configuration (tenant_id, content_type, resource_name, resource_content) VALUES(0, 'PLATFORM_PORTAL', 'security-config.properties', ? ) ", [data])
        def warnAboutCSRF = new WarnAboutCSRF()

        when:
        warnAboutCSRF.execute(migrationContext)

        then:
        if (expectedWarning) {
            warnAboutCSRF.warning == WarnAboutCSRF.WARN_MESSAGE_CSRF
        } else {
            warnAboutCSRF.warning == null
        }

        where:
        propertiesContent             | expectedWarning | description
        "security.csrf.enabled false" | true            | "CSRF is disabled"
        "security.csrf.enabled true"  | false           | "CSRF is enabled"
        "dummy property true"         | true            | "CSRF property is missing"
    }

    def "should warn when no row are found "() {
        setup:
        def warnAboutCSRF = new WarnAboutCSRF()

        when:
        warnAboutCSRF.execute(migrationContext)

        then:

        warnAboutCSRF.getWarning() == WarnAboutCSRF.WARN_MESSAGE_CSRF
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["configuration"] as String[])
    }


}
