/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.migration.core

import groovy.sql.Sql
import org.bonitasoft.migration.core.database.DatabaseHelper
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Baptiste Mesta
 */
class MigrationRunnerTest extends Specification {

    def logger = Spy(Logger.class)
    VersionMigration versionMigration = Mock(VersionMigration)
    MigrationStep migrationStep1 = Mock(MigrationStep)
    MigrationStep migrationStep2 = Mock(MigrationStep)
    List<MigrationStep> migrationStepList = [migrationStep1, migrationStep2]
    MigrationContext migrationContext = new MigrationContext()
    DatabaseHelper databaseHelper = Mock(DatabaseHelper)
    MigrationRunner migrationRunner
    Sql sql = Mock(Sql)
    DisplayUtil displayUtil = Mock(DisplayUtil)

    def setup() {
        versionMigration.getMigrationSteps() >> migrationStepList
        migrationContext.sql = ThreadLocal.<Sql> withInitial({ sql })
        migrationContext.databaseHelper = databaseHelper
        migrationRunner = new MigrationRunner(migrationVersions: [versionMigration], context: migrationContext, logger: logger, displayUtil: displayUtil)
    }

    @Unroll
    "run should #migrateText execute migrate bonita home for version #version"() {
        given:
        versionMigration.getVersion() >> version
        when:
        migrationRunner.run(true)
        then:
        migrate * versionMigration.migrateBonitaHome(_)
        where:
        version || migrate
        "7.2.8" || 1
        "7.3.0" || 0
        "7.3.1" || 0
        migrateText = migrate == 1 ? "" : "not"
    }

    def "run should execute migration steps in order"() {
        given:
        versionMigration.getVersion() >> "7.3.1"
        when:
        migrationRunner.run(true)
        then:
        1 * migrationStep1.execute(migrationContext)
        then:
        1 * migrationStep2.execute(migrationContext)
    }

    def "run should change platform version in database"() {
        given:
        versionMigration.version >> "7.3.1"
        when:
        migrationRunner.run(true)
        then:
        1 * sql.executeUpdate("UPDATE platform SET previousVersion = version")
        1 * sql.executeUpdate("UPDATE platform SET version = ${"7.3.1"}")
    }

    def "run check pre-requisites before running migration"() {
        setup:
        def versionMigration_7_5_0 = Mock(VersionMigration)
        versionMigration_7_5_0.version >> "7.5.0"
        versionMigration_7_5_0.getPreMigrationWarnings(migrationContext) >> ["some message1", "some message2"]
        versionMigration_7_5_0.getMigrationSteps() >> []

        // so that we don't get asked for confirmation:
        System.setProperty("auto.accept", "true")

        MigrationRunner migrationRunner = new MigrationRunner(migrationVersions: [versionMigration_7_5_0], context: migrationContext, logger: logger, displayUtil: displayUtil)

        when:
        migrationRunner.run(false)

        then:
        1 * displayUtil.logWarningsInRectangleWithTitle("Migration to version 7.5.0", "some message1", "some message2")
    }

    def "run check blocking pre-requisites before running migration"() {
        setup:
        def versionMigration_7_8_0 = Mock(VersionMigration)
        versionMigration_7_8_0.version >> "7.8.0"
        versionMigration_7_8_0.getPreMigrationBlockingMessages(migrationContext) >> ["Blocking Message"]
        versionMigration_7_8_0.getMigrationSteps() >> []

        MigrationRunner migrationRunner = new MigrationRunner(migrationVersions: [versionMigration_7_8_0], context: migrationContext, logger: logger, displayUtil: displayUtil)

        when:
        migrationRunner.run(false)

        then:
        1 * displayUtil.logWarningsInRectangleWithTitle("Migration to version 7.8.0", ["Blocking Message"] as String[])
    }

    def "should gather ALL pre-requisites before asking for confirmation"() {
        setup:
        def versionMigration_7_4_9 = Mock(VersionMigration)
        versionMigration_7_4_9.version >> "7.4.9"
        versionMigration_7_4_9.getPreMigrationWarnings(migrationContext) >> ["Warning 7.4.9"]
        versionMigration_7_4_9.getMigrationSteps() >> []
        def versionMigration_7_5_0 = Mock(VersionMigration)
        versionMigration_7_5_0.version >> "7.5.0"
        versionMigration_7_5_0.getPreMigrationWarnings(migrationContext) >> ["Warning 7.5.0"]
        versionMigration_7_5_0.getMigrationSteps() >> []

        // so that we don't get asked for confirmation:
        System.setProperty("auto.accept", "true")

        MigrationRunner migrationRunner1 = new MigrationRunner(migrationVersions: [versionMigration_7_4_9, versionMigration_7_5_0], context: migrationContext, logger: logger, displayUtil: displayUtil)

        when:
        migrationRunner1.run(false)

        // Several 'then' to verify the order of execution:
        then:
        1 * displayUtil.logWarningsInRectangleWithTitle("Migration to version 7.4.9", ["Warning 7.4.9"] as String[])

        then:
        1 * displayUtil.logWarningsInRectangleWithTitle("Migration to version 7.5.0", ["Warning 7.5.0"] as String[])

        then:
        1 * logger.info("Execute migration to version 7.4.9")
        1 * logger.info("Execute migration to version 7.5.0")
    }

    def "should warn when VERSION_OVERRIDDEN defined and not VERSION_OVERRIDE_BY"() {
        setup:
        System.setProperty(MigrationRunner.VERSION_OVERRIDDEN, "crush")
        System.clearProperty(MigrationRunner.VERSION_OVERRIDE_BY)

        when:
        migrationRunner.checkOverrideValidity()

        then:
        1 * logger.warn("System properties '$MigrationRunner.VERSION_OVERRIDDEN' and '$MigrationRunner.VERSION_OVERRIDE_BY' must be set together")
    }

    def "should warn when VERSION_OVERRIDE_BY defined and not VERSION_OVERRIDDEN"() {
        setup:
        System.clearProperty(MigrationRunner.VERSION_OVERRIDDEN)
        System.setProperty(MigrationRunner.VERSION_OVERRIDE_BY, "candy")

        when:
        migrationRunner.checkOverrideValidity()

        then:
        1 * logger.warn("System properties '$MigrationRunner.VERSION_OVERRIDDEN' and '$MigrationRunner.VERSION_OVERRIDE_BY' must be set together")
    }

    def "should override new version to VERSION_OVERRIDE_BY"() {
        setup:
        System.setProperty(MigrationRunner.VERSION_OVERRIDDEN, "7.5.1")
        System.setProperty(MigrationRunner.VERSION_OVERRIDE_BY, "7.5.1.RC-02")
        migrationRunner.checkOverrideValidity()

        when:
        migrationRunner.changePlatformVersion(sql, "7.5.1")

        then:
        1 * logger.info("Overriding version 7.5.1 by 7.5.1.RC-02")
        1 * logger.info("Platform version in database is now 7.5.1.RC-02")
    }

    def "should not override new version if VERSION_OVERRIDE_BY does not match"() {
        setup:
        System.setProperty(MigrationRunner.VERSION_OVERRIDDEN, "7.5.9")
        System.setProperty(MigrationRunner.VERSION_OVERRIDE_BY, "7.5.1.RC-02")
        migrationRunner.checkOverrideValidity()

        when:
        migrationRunner.changePlatformVersion(sql, "7.5.3")

        then:
        0 * logger.info("Overriding version 7.5.1 by 7.5.1.RC-02")
        1 * logger.info("Platform version in database is now 7.5.3")
    }

    public static final List<String> GLOBAL_POST_MIGRATION_WARNINGS = ['Archive contract data table backup had been created ("arch_contract_data_backup") as its model migration is time consuming.',
                                                                       'All this information is not required by Bonita to work and does not affect user experience,',
                                                                       'but it keeps the information of all contracts sent to execute tasks or instantiate processes.',
                                                                       'Based on your needs, this information can be migrated into the original table using the tool',
                                                                       '(please run live-migration script located in tools/live-migration directory) while bonita platform is up & running',
                                                                       'or dropped to reduce disk space']

    def "should log warning message if arch_contract_data_backup table exists"() {
        given:
        versionMigration.getVersion() >> "7.9.0"
        databaseHelper.hasTable("arch_contract_data_backup") >> true

        when:
        migrationRunner.run(false)

        then:
        1 * displayUtil.logWarningsInRectangleWithTitle("Global post-migration warning", GLOBAL_POST_MIGRATION_WARNINGS)
    }

    def "should NOT log warning message if arch_contract_data_backup table does not exist"() {
        given:
        versionMigration.getVersion() >> "7.9.0"
        databaseHelper.hasTable("arch_contract_data_backup") >> false

        when:
        migrationRunner.run(false)

        then:
        0 * displayUtil.logWarningsInRectangleWithTitle("Global post-migration warning", GLOBAL_POST_MIGRATION_WARNINGS)
    }

}

