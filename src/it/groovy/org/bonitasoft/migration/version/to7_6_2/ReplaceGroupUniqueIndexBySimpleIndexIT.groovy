package org.bonitasoft.migration.version.to7_6_2

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Danila Mazour
 */
class ReplaceGroupUniqueIndexBySimpleIndexIT extends Specification {

    @Shared
    Logger logger = new Logger()


    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    private ReplaceGroupUniqueIndexBySimpleIndex migrationStep = new ReplaceGroupUniqueIndexBySimpleIndex()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_6_2/group")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["group_"] as String[])
    }


    def "should replace unique constraint by an index"() {
        given:
        assert dbUnitHelper.hasUniqueKeyOnTable("group_", migrationContext.databaseHelper.getUniqueKeyNameOnTable("group_"))

        when:
        migrationStep.execute(migrationContext)

        then:
        assert !dbUnitHelper.hasUniqueKeyOnTable("group_", migrationContext.databaseHelper.getUniqueKeyNameOnTable("group_"))
        assert dbUnitHelper.hasIndexOnTable("group_", "idx_group_name")


    }
}
