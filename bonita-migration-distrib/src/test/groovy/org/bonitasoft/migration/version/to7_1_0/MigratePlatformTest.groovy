package org.bonitasoft.migration.version.to7_1_0
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.database.DatabaseHelper
import spock.lang.Specification
/**
 * @author Laurent Leseigneur
 */
class MigratePlatformTest extends Specification {

    def "migratePlatform" () {
        def platform = new MigratePlatform()
        def migrationContext= Mock(MigrationContext)
        def dbHelper = Mock(DatabaseHelper)
        migrationContext.getDatabaseHelper() >> dbHelper

        when:
        platform.execute(migrationContext)

        then:
        1 * dbHelper.executeScript("MigratePlatform", "platform")
    }
}
