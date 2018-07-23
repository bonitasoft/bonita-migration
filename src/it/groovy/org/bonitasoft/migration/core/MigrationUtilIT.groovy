/*
 * Copyright (C) 2018 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.migration.core

import static org.assertj.core.api.Assertions.assertThat

import org.bonitasoft.migration.DBUnitHelper
import spock.lang.Shared
import spock.lang.Specification

class MigrationUtilIT extends Specification {

    @Shared
    Logger logger = new Logger()

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def setup() {
        dropTestTables()
        String folder = 'core/platform'
        logger.info("Create tables from sql file in $folder")
        dbUnitHelper.executeScript(DBUnitHelper.class.getClassLoader().getResource("sql/$folder/${migrationContext.dbVendor.name().toLowerCase()}.sql"))
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(['platform'] as String[])
    }


    def "should detect Community Edition based on values in the Platform table"() {
        setup:
        migrationContext.sql.execute("""
            INSERT INTO platform(id, version, previousVersion, initialVersion, created, createdBy, information)
            VALUES (1, '7.4.0', '7.2.2', '7.1.0', 1532016759488, 'platformAdmin', null)
        """)

        when:
        Edition bonitaEdition = MigrationUtil.getEdition(migrationContext.sql)

        then:
        assertThat(bonitaEdition).describedAs("Bonita Edition").isEqualTo(Edition.COMMUNITY)
    }

    def "should detect Subscription Edition based on values in the Platform table"() {
        setup:
        migrationContext.sql.execute("""
            INSERT INTO platform(id, version, previousVersion, initialVersion, created, createdBy, information)
            VALUES (1, '7.6.1', '7.4.2', '7.0.1', 1532016759488, 'platformAdmin', 'tFy+1JzBdl1xWSFyAT4/YwJyCNjo1zgEVUA6xvxlQUCQjrVs/BXJqg==')
        """)

        when:
        Edition bonitaEdition = MigrationUtil.getEdition(migrationContext.sql)

        then:
        assertThat(bonitaEdition).describedAs("Bonita Edition").isEqualTo(Edition.SUBSCRIPTION)
    }

}
