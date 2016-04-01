package org.bonitasoft.migration.version.to7_2_0

import groovy.sql.Sql
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.database.DatabaseHelper
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Emmanuel Duchastenier
 */
class BARInDatabaseTest extends Specification {

    def context = Mock(MigrationContext)

    def logger = Mock(Logger)

    def sql = Mock(Sql)

    def databaseHelper = Mock(DatabaseHelper)

    def setup() {
        databaseHelper.sql >> sql
        context.databaseHelper >> databaseHelper
        context.logger >> logger
        context.sql >> sql
    }

    @Unroll
    def "migrating BAR files to database should not raise exception if #bonitaHome folder"(String bonitaHome) {

        setup:
        BARInDatabase barInDatabase = new BARInDatabase()

        def file = new File(this.class.getResource("/$bonitaHome").file)
        logger.info file.getAbsolutePath()
        context.bonitaHome >> file

        when:
        barInDatabase.execute(context)

        then:
        0 * logger.info({ it ==~ /put .* initial document.*/ })

        where:
        bonitaHome << ["no-processes", "empty-processes"]

    }
}
