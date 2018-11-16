package org.bonitasoft.migration.version.to7_0_1

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

import static org.assertj.core.api.Assertions.assertThat

/**
 * @author Laurent Leseigneur
 */
class DefaultPortalThemeIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    def setup() {
        migrationContext.setVersion("7.0.1")
        dropTestTables()
        dbUnitHelper.createTables("7_0_0/theme")

    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["theme"] as String[])
    }

    def "should migrate css for portal default theme"() {
        setup:
        migrationContext.sql.execute("""
            INSERT INTO theme(tenantid, id, isdefault, content, csscontent, type, lastupdatedate)
            VALUES (1,2,${true}, ${"content".bytes},${"csscontent".bytes},${"PORTAL"},50)
        """)

        migrationContext.sql.execute("""
            INSERT INTO theme(tenantid, id, isdefault, content, csscontent, type, lastupdatedate)
            VALUES (1,3,${false}, ${"content".bytes},${"csscontent".bytes},${"PORTAL"},51)
        """)

        migrationContext.sql.execute("""
            INSERT INTO theme(tenantid, id, isdefault, content, csscontent, type, lastupdatedate)
            VALUES (1,4,${false}, ${"mobile".bytes},null,${"MOBILE"},52)
        """)

        when:
        new UpdateDefaultPortalTheme().execute(migrationContext)

        then:
        def rowDefaultPortalTheme = migrationContext.sql.firstRow("SELECT tenantid, id, isdefault, content, csscontent, type, lastupdatedate FROM theme WHERE id = 2")
        assertThat(migrationContext.databaseHelper.getBlobContentAsString(rowDefaultPortalTheme.csscontent)).as("should retrieve css file").startsWith("/* Eric Meyer's Reset CSS v2.0 - http://cssreset.com */")

        def rowCustomTheme = migrationContext.sql.firstRow("SELECT tenantid, id, isdefault, content, csscontent, type, lastupdatedate FROM theme WHERE id = 3")
        assertThat(migrationContext.databaseHelper.getBlobContentAsString(rowCustomTheme.csscontent)).as("should not modify custom css theme ").isEqualTo("csscontent")

        def rowMobile = migrationContext.sql.firstRow("SELECT tenantid, id, isdefault, content, csscontent, type, lastupdatedate FROM theme WHERE id = 4")
        assertThat(rowMobile.csscontent).as("should not update default mobile theme").isNull()
        assertThat(new BigDecimal(rowMobile.lastupdatedate).longValue()).as("should not update mobile theme last update date").isEqualTo(52L)

    }


}
