package org.bonitasoft.migration.version.to7_4_0

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.database.DatabaseHelper
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Laurent Leseigneur
 */
class WarnAboutCSRFTest extends Specification {

    def logger = Mock(Logger)

    def context = Mock(MigrationContext)

    def sql = Mock(Sql)

    def row = Mock(GroovyRowResult)

    def databaseHelper = Mock(DatabaseHelper)

    def WarnAboutCSRF migrationStep

    def setup() {
        databaseHelper.sql >> sql
        context.sql >> sql
        context.databaseHelper >> databaseHelper
        context.logger >> logger
    }

    @Unroll
    def "should warn be #expectedWarning when #description"(
            def propertiesContent, def expectedWarning, def description) {
        setup:
        row.content >> propertiesContent.bytes
        sql.firstRow(WarnAboutCSRF.QUERY_PLATFORM_SECURITY_CONFIG) >> row
        databaseHelper.getBlobContentAsString(row.content) >> propertiesContent
        migrationStep = new WarnAboutCSRF()

        when:
        migrationStep.execute(context)

        then:
        if (expectedWarning) {
            migrationStep.warning == WarnAboutCSRF.WARN_MESSAGE_CSRF
        } else {
            migrationStep.warning == null
        }

        where:
        propertiesContent             | expectedWarning | description
        "security.csrf.enabled false" | true            | "CSRF is disabled"
        "security.csrf.enabled true"  | false           | "CSRF is enabled"
        "dummy property true"         | true            | "CSRF property is missing"


    }

    def "should warn when no row are found "() {
        setup:
        sql.firstRow(WarnAboutCSRF.QUERY_PLATFORM_SECURITY_CONFIG) >> null
        migrationStep = new WarnAboutCSRF()

        when:
        migrationStep.execute(context)

        then:
        migrationStep.warning == WarnAboutCSRF.WARN_MESSAGE_CSRF

    }

}
