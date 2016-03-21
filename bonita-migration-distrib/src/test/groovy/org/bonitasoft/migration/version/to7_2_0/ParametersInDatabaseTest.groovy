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
class ParametersInDatabaseTest extends Specification {

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
    def "migrating parameters to database should not raise exception if #bonitaHome folder"(String bonitaHome) {

        setup:
        ParametersInDatabase parametersInDatabase = new ParametersInDatabase()

        def file = new File(this.class.getResource("/$bonitaHome").file)
        logger.info file.getAbsolutePath()
        context.bonitaHome >> file

        when:
        parametersInDatabase.execute(context)

        then:
        0 * logger.info({ it ==~ /Putting .* parameters in database for process .*/ })

        where:
        bonitaHome << ["no-processes", "empty-processes"]

    }
}
