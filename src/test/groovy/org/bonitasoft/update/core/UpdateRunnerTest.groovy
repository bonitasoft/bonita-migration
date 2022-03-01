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

package org.bonitasoft.update.core

import groovy.sql.Sql
import org.bonitasoft.update.core.database.DatabaseHelper
import spock.lang.Specification
/**
 * @author Baptiste Mesta
 */
class UpdateRunnerTest extends Specification {

    def logger = Spy(Logger.class)
    VersionUpdate versionUpdate = Mock(VersionUpdate)
    UpdateStep updateStep1 = Mock(UpdateStep)
    UpdateStep updateStep2 = Mock(UpdateStep)
    List<UpdateStep> updateStepList = [updateStep1, updateStep2]
    UpdateContext updateContext = new UpdateContext()
    DatabaseHelper databaseHelper = Mock(DatabaseHelper)
    UpdateRunner updateRunner
    Sql sql = Mock(Sql)
    DisplayUtil displayUtil = Mock(DisplayUtil)

    def setup() {
        versionUpdate.getUpdateSteps() >> updateStepList
        updateContext.sql = ThreadLocal.<Sql> withInitial({ sql })
        updateContext.databaseHelper = databaseHelper
        updateRunner = new UpdateRunner(versionUpdates: [versionUpdate], context: updateContext, logger: logger, displayUtil: displayUtil)
    }

    def "run should execute update steps in order"() {
        given:
        versionUpdate.getVersion() >> "7.3.1"
        when:
        updateRunner.run(true)
        then:
        1 * updateStep1.execute(updateContext)
        then:
        1 * updateStep2.execute(updateContext)
    }

    def "run should change platform version in database"() {
        given:
        versionUpdate.version >> "7.3.1"
        when:
        updateRunner.run(true)
        then:
        1 * sql.executeUpdate("UPDATE platform SET previousVersion = version, version = ${"7.3.1"}")
    }

    def "run check pre-requisites before running update"() {
        setup:
        def twoDigitVersion = "7.14"
        def versionUpdate = Mock(VersionUpdate)
        versionUpdate.version >> "${twoDigitVersion}.0"
        versionUpdate.getPreUpdateWarnings(updateContext) >> ["some message1", "some message2"]
        versionUpdate.getUpdateSteps() >> []

        // so that we don't get asked for confirmation:
        System.setProperty("auto.accept", "true")

        UpdateRunner updateRunner = new UpdateRunner(versionUpdates: [versionUpdate], context: updateContext, logger: logger, displayUtil: displayUtil)

        when:
        updateRunner.run(false)

        then:
        1 * displayUtil.logWarningsInRectangleWithTitle("Update to version ${twoDigitVersion}", "some message1", "some message2")
    }

    def "run check blocking pre-requisites before running update"() {
        setup:
        def versionUpdate_7_8_0 = Mock(VersionUpdate)
        versionUpdate_7_8_0.version >> "7.8.0"
        versionUpdate_7_8_0.getPreUpdateBlockingMessages(updateContext) >> ["Blocking Message"]
        versionUpdate_7_8_0.getUpdateSteps() >> []

        UpdateRunner updateRunner = new UpdateRunner(versionUpdates: [versionUpdate_7_8_0], context: updateContext, logger: logger, displayUtil: displayUtil)

        when:
        updateRunner.run(false)

        then:
        1 * displayUtil.logWarningsInRectangleWithTitle("Update to version 7.8", ["Blocking Message"] as String[])
    }

    def "should gather ALL pre-requisites before asking for confirmation"() {
        setup:
        def versionUpdate_7_4_9 = Mock(VersionUpdate)
        versionUpdate_7_4_9.version >> "7.4.9"
        versionUpdate_7_4_9.getPreUpdateWarnings(updateContext) >> ["Warning 7.4.9"]
        versionUpdate_7_4_9.getUpdateSteps() >> []
        def versionUpdate_7_5_0 = Mock(VersionUpdate)
        versionUpdate_7_5_0.version >> "7.5.0"
        versionUpdate_7_5_0.getPreUpdateWarnings(updateContext) >> ["Warning 7.5.0"]
        versionUpdate_7_5_0.getUpdateSteps() >> []
        def versionUpdate_7_13_0 = Mock(VersionUpdate)
        versionUpdate_7_13_0.version >> "7.13.0"
        versionUpdate_7_13_0.getPreUpdateWarnings(updateContext) >> ["Warning 7.13.0"]
        versionUpdate_7_13_0.getUpdateSteps() >> []

        // so that we don't get asked for confirmation:
        System.setProperty("auto.accept", "true")

        UpdateRunner updateRunner1 = new UpdateRunner(versionUpdates: [versionUpdate_7_4_9, versionUpdate_7_5_0, versionUpdate_7_13_0], context: updateContext, logger: logger, displayUtil: displayUtil)

        when:
        updateRunner1.run(false)

        // Several 'then' to verify the order of execution:
        then:
        1 * displayUtil.logWarningsInRectangleWithTitle("Update to version 7.4", ["Warning 7.4.9"] as String[])

        then:
        1 * displayUtil.logWarningsInRectangleWithTitle("Update to version 7.5", ["Warning 7.5.0"] as String[])

        then:
        1 * displayUtil.logWarningsInRectangleWithTitle("Update to version 7.13", ["Warning 7.13.0"] as String[])

        then:
        1 * logger.info("Execute update to version 7.4")
        1 * logger.info("Execute update to version 7.5")
        1 * logger.info("Execute update to version 7.13")

    }

    def "should warn when VERSION_OVERRIDDEN defined and not VERSION_OVERRIDE_BY"() {
        setup:
        System.setProperty(UpdateRunner.VERSION_OVERRIDDEN, "crush")
        System.clearProperty(UpdateRunner.VERSION_OVERRIDE_BY)

        when:
        updateRunner.checkOverrideValidity()

        then:
        1 * logger.warn("System properties '$UpdateRunner.VERSION_OVERRIDDEN' and '$UpdateRunner.VERSION_OVERRIDE_BY' must be set together")
    }


    def "should warn when VERSION_OVERRIDE_BY defined and not VERSION_OVERRIDDEN"() {
        setup:
        System.clearProperty(UpdateRunner.VERSION_OVERRIDDEN)
        System.setProperty(UpdateRunner.VERSION_OVERRIDE_BY, "candy")

        when:
        updateRunner.checkOverrideValidity()

        then:
        1 * logger.warn("System properties '$UpdateRunner.VERSION_OVERRIDDEN' and '$UpdateRunner.VERSION_OVERRIDE_BY' must be set together")
    }

    def "should override new version to VERSION_OVERRIDE_BY"() {
        setup:
        System.setProperty(UpdateRunner.VERSION_OVERRIDDEN, "7.5.1")
        System.setProperty(UpdateRunner.VERSION_OVERRIDE_BY, "7.5.1.RC-02")
        updateRunner.checkOverrideValidity()

        when:
        updateRunner.changePlatformVersion(sql, "7.5.1")

        then:
        1 * logger.info("Overriding version 7.5.1 by 7.5.1.RC-02")
        1 * logger.info("Platform version in database is now 7.5.1.RC-02")
    }

    def "should not override new version if VERSION_OVERRIDE_BY does not match"() {
        setup:
        System.setProperty(UpdateRunner.VERSION_OVERRIDDEN, "7.5.9")
        System.setProperty(UpdateRunner.VERSION_OVERRIDE_BY, "7.5.1.RC-02")
        updateRunner.checkOverrideValidity()

        when:
        updateRunner.changePlatformVersion(sql, "7.5.3")

        then:
        0 * logger.info("Overriding version 7.5.1 by 7.5.1.RC-02")
        1 * logger.info("Platform version in database is now 7.5.3")
    }

    public static final List<String> GLOBAL_POST_UPDATE_WARNINGS = ['Archive contract data table backup had been created ("arch_contract_data_backup") as its model update is time consuming.',
                                                                       'All this information is not required by Bonita to work and does not affect user experience,',
                                                                       'but it keeps the information of all contracts sent to execute tasks or instantiate processes.',
                                                                       'Based on your needs, this information can be updated into the original table using the tool',
                                                                       '(please run live-migration tool available on Bonitasoft Customer Portal) while bonita platform is up & running',
                                                                       'or dropped to reduce disk space']

    def "should log warning message if arch_contract_data_backup table exists"() {
        given:
        versionUpdate.getVersion() >> "7.9.0"
        databaseHelper.hasTable("arch_contract_data_backup") >> true

        when:
        updateRunner.run(false)

        then:
        1 * displayUtil.logWarningsInRectangleWithTitle("Global post-update warning", GLOBAL_POST_UPDATE_WARNINGS)
    }

    def "should NOT log warning message if arch_contract_data_backup table does not exist"() {
        given:
        versionUpdate.getVersion() >> "7.9.0"
        databaseHelper.hasTable("arch_contract_data_backup") >> false

        when:
        updateRunner.run(false)

        then:
        0 * displayUtil.logWarningsInRectangleWithTitle("Global post-update warning", GLOBAL_POST_UPDATE_WARNINGS)
    }

}

