package org.bonitasoft.migration.version.to7_2_0

import groovy.sql.Sql
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.database.DatabaseHelper
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Laurent Leseigneur
 */
class CheckBDMQueriesTest extends Specification {

    def context = Mock(MigrationContext)

    def logger = Mock(Logger)

    def sql = Mock(Sql)

    def databaseHelper = Mock(DatabaseHelper)

    def setup() {
        databaseHelper.sql >> sql
        context.databaseHelper >> databaseHelper
        context.logger >> logger
    }

    @Unroll
    def "should verify BDM when bonita home is #bonitaHome "(String bonitaHome, int tenantId, int expectedWarningMessageCount) {

        setup:
        CheckBDMQueries checkBDMQueries = new CheckBDMQueries()
        def results = new ArrayList<>()
        results.add(["id": tenantId, "name": bonitaHome, "status": "ACTIVATED"])
        databaseHelper.sql.rows("select t.id,t.name, t.status from tenant t order by t.id") >> results

        def file = new File(this.class.getResource("/$bonitaHome").file)

        logger.info file.getAbsolutePath()
        context.bonitaHome >> file

        when:
        checkBDMQueries.execute(context)

        then:
        1 * logger.info("analyzing tenant [id:$tenantId, name:$bonitaHome, status:ACTIVATED]")
        expectedWarningMessageCount * logger.warn({ it ==~ /Business Object .* has a custom query named countFor.*\. This may conflict with default countFor queries\./ })

        where:
        bonitaHome | tenantId || expectedWarningMessageCount
        "withBDM"  | 1        || 3
        "noBDM"    | 2        || 0

    }
}
