/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

    private static final DEFAULT_TWO_DIGIT_VERSION = "1.2"
    private static final DEFAULT_THREE_DIGIT_VERSION = "${DEFAULT_TWO_DIGIT_VERSION}.3"

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
        versionUpdate.version >> DEFAULT_THREE_DIGIT_VERSION
        updateContext.sql = ThreadLocal.<Sql> withInitial({ sql })
        updateContext.databaseHelper = databaseHelper
        updateRunner = new UpdateRunner(versionUpdates: [versionUpdate], context: updateContext, logger: logger, displayUtil: displayUtil)
    }

    def "run should execute update steps in order"() {
        when:
        updateRunner.run(true)
        then:
        1 * updateStep1.execute(updateContext)
        then:
        1 * updateStep2.execute(updateContext)
    }

    def "run should change platform version in database"() {
        when:
        updateRunner.run(true)
        then:
        1 * sql.executeUpdate("UPDATE platform SET previousVersion = version, version = ${DEFAULT_THREE_DIGIT_VERSION}")
    }

    def "run check pre-requisites before running update"() {
        given:
        versionUpdate.getPreUpdateWarnings(updateContext) >> ["some message1", "some message2"]
        versionUpdate.getUpdateSteps() >> []

        // so that we don't get asked for confirmation:
        System.setProperty("auto.accept", "true")

        when:
        updateRunner.run(false)

        then:
        1 * displayUtil.logWarningsInRectangleWithTitle("Update to version ${DEFAULT_TWO_DIGIT_VERSION}", "some message1", "some message2")
    }

    def "run check blocking pre-requisites before running update"() {
        given:
        versionUpdate.getPreUpdateBlockingMessages(updateContext) >> ["Blocking Message"]
        versionUpdate.getUpdateSteps() >> []

        when:
        updateRunner.run(false)

        then:
        1 * displayUtil.logWarningsInRectangleWithTitle("Update to version ${DEFAULT_TWO_DIGIT_VERSION}", ["Blocking Message"] as String[])
    }

    def "should gather ALL pre-requisites before asking for confirmation"() {
        given:
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
        given:
        System.setProperty(UpdateRunner.VERSION_OVERRIDDEN, "crush")
        System.clearProperty(UpdateRunner.VERSION_OVERRIDE_BY)

        when:
        updateRunner.checkOverrideValidity()

        then:
        1 * logger.warn("System properties '$UpdateRunner.VERSION_OVERRIDDEN' and '$UpdateRunner.VERSION_OVERRIDE_BY' must be set together")
    }


    def "should warn when VERSION_OVERRIDE_BY defined and not VERSION_OVERRIDDEN"() {
        given:
        System.clearProperty(UpdateRunner.VERSION_OVERRIDDEN)
        System.setProperty(UpdateRunner.VERSION_OVERRIDE_BY, "candy")

        when:
        updateRunner.checkOverrideValidity()

        then:
        1 * logger.warn("System properties '$UpdateRunner.VERSION_OVERRIDDEN' and '$UpdateRunner.VERSION_OVERRIDE_BY' must be set together")
    }

    def "should override new version to VERSION_OVERRIDE_BY"() {
        given:
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
        given:
        System.setProperty(UpdateRunner.VERSION_OVERRIDDEN, "7.5.9")
        System.setProperty(UpdateRunner.VERSION_OVERRIDE_BY, "7.5.1.RC-02")
        updateRunner.checkOverrideValidity()

        when:
        updateRunner.changePlatformVersion(sql, "7.5.3")

        then:
        0 * logger.info("Overriding version 7.5.1 by 7.5.1.RC-02")
        1 * logger.info("Platform version in database is now 7.5.3")
    }

    def "should log warning message if arch_contract_data_backup table exists"() {
        given:
        databaseHelper.hasTable("arch_contract_data_backup") >> true

        when:
        updateRunner.run(false)

        then:
        1 * displayUtil.logWarningsInRectangleWithTitle("Global post-update warnings", UpdateUtil.ARCH_CONTRACT_DATA_BACKUP_GLOBAL_MSG)
    }

    def "should NOT log warning message if arch_contract_data_backup table does not exist"() {
        given:
        databaseHelper.hasTable("arch_contract_data_backup") >> false

        when:
        updateRunner.run(false)

        then:
        0 * displayUtil.logWarningsInRectangleWithTitle("Global post-update warnings", UpdateUtil.ARCH_CONTRACT_DATA_BACKUP_GLOBAL_MSG)
    }

    def "run step warnings after running step update"() {
        given:
        updateStep1.description >> "updateStep1"
        updateStep1.warning >> "warning step 1"
        updateStep2.description >> "updateStep2"
        updateStep2.warning >> "warning step 2"

        when:
        updateRunner.run(false)

        then:
        1 * displayUtil.logWarningsInRectangleWithTitle("Update to version: ${DEFAULT_TWO_DIGIT_VERSION} - step: updateStep1", "warning step 1")
        1 * displayUtil.logWarningsInRectangleWithTitle("Update to version: ${DEFAULT_TWO_DIGIT_VERSION} - step: updateStep2", "warning step 2")
    }

    def "run post-update warnings after running update"() {
        given:
        versionUpdate.getPostUpdateWarnings(updateContext) >> ["post-update warning 1", "post-update warning 2"]
        versionUpdate.getUpdateSteps() >> []

        when:
        updateRunner.run(false)

        then:
        1 * displayUtil.logWarningsInRectangleWithTitle("Post-update to version: ${DEFAULT_TWO_DIGIT_VERSION}", "post-update warning 1", "post-update warning 2")
    }
}
