/**
 * Copyright (C) 2024 Bonitasoft S.A.
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

import com.github.zafarkhaja.semver.Version
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Baptiste Mesta
 */
class UpdateTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    private def infos = []
    private def logger = [info: { String message -> infos.add(message) }] as Logger
    private Sql sql = Mock(Sql)
    private UpdateContext updateContext = Mock(UpdateContext)
    private DisplayUtil displayUtil = Mock(DisplayUtil)
    private Update update
    private UpdateRunner runner = Mock(UpdateRunner)

    def setup() {
        updateContext.sql >> sql
        updateContext.logger >> logger
        update = Spy(Update, constructorArgs: [updateContext, displayUtil])
        update.createRunner() >> runner
    }

    def "update with an unhandled source version should throw exception"() {
        given:
        versionInDatabase("7.9.0")
        targetVersion("7.11.0")
        when:
        update.run(false)
        then:
        IllegalStateException throwable = thrown()
        // Should we rename also migration tool v2 to update tool v2?
        throwable.message == "Sorry, but this tool can't manage version before 7.10.0, use the migration tool version 2"
    }

    def "update with an unhandled target version should throw exception"() {
        given:
        versionInDatabase("7.11.0")
        targetVersion("7.25.0")
        when:
        update.run(false)
        then:
        IllegalStateException throwable = thrown()
        throwable.message == "7.25.0 is not yet handled by this version of the update tool"
    }

    def "update with a version before 7.0 should throw exception"() {
        given:
        versionInDatabase("6.5.0")
        targetVersion("7.11.0")
        when:
        update.run(false)
        then:
        IllegalStateException throwable = thrown()
        // Should we rename also migration tool v2 to update tool v2?
        throwable.message == "Sorry, but this tool can't manage version before 7.10.0, use the migration tool version 2"
    }

    def "same target and source version should throw exception"() {
        given:
        versionInDatabase("7.11.0")
        targetVersion("7.11.0")
        when:
        update.run(false)
        then:
        IllegalStateException throwable = thrown()
        throwable.message == "The version is already in 7.11"
    }


    def "target before source version should throw exception"() {
        given:
        versionInDatabase("7.12.0")
        targetVersion("7.11.0")
        when:
        update.run(false)
        then:
        IllegalStateException throwable = thrown()
        throwable.message == "The target version 7.11.0 can not be before source version 7.12"
    }

    @Unroll
    def "should update from #source to #target execute steps #versionUpdates"() {
        given:
        versionInDatabase(source)
        targetVersion(target)
        when:
        update.run(false)
        then:
        1 * runner.setVersionUpdates({
            it.collect { it.class.name.split('\\.').last() } == versionUpdates
        })

        where:
        source   | target   || versionUpdates
        "7.10.0" | "7.11.0" || ["UpdateTo7_11_0"]
        "7.10.2" | "7.12.0" || ["UpdateTo7_11_0", "UpdateTo7_12_0"]
        "7.10.3" | "7.13.0" || ["UpdateTo7_11_0", "UpdateTo7_12_0", "UpdateTo7_13_0"]
    }

    // Those 2 tests are commented because there are no "forbidden" version right now,
    // but we may want to add some in the future, as the mechanism is already in place.
    // See Update.TRANSITION_VERSIONS for details:
    //    def "should NOT throw exception if trying to update to 7.7.0 and SysProp \"ignore.invalid.target.version\" provided"() {
    //        given:
    //        versionInDatabase("7.6.3")
    //        targetVersion("7.7.0")
    //        System.setProperty("ignore.invalid.target.version", "value")
    //
    //        when:
    //        update.run(false)
    //
    //        then:
    //        noExceptionThrown()
    //
    //        cleanup:
    //        System.clearProperty("ignore.invalid.target.version")
    //    }

    //    def "should INDEED throw exception if trying to update to 7.6.9 and SysProp \"ignore.invalid.target.version\" provided but 7.6.9 not in invisible TRANSITION VERSION list"() {
    //        given:
    //        versionInDatabase("7.6.3")
    //        targetVersion("7.6.9")
    //        System.setProperty("ignore.invalid.target.version", "value")
    //
    //        when:
    //        update.run(false)
    //
    //        then:
    //        IllegalStateException throwable = thrown()
    //        throwable.message == "7.6.9 is not yet handled by this version of the update tool"
    //
    //        cleanup:
    //        System.clearProperty("ignore.invalid.target.version")
    //    }

    def "should allow to update between 2 version handled by the tool"() {
        given:
        versionInDatabase("7.11.0")
        targetVersion("7.12.0")

        when:
        update.run(true)

        then:
        noExceptionThrown()
    }


    protected void targetVersion(String s) {
        updateContext.targetVersion >> Version.valueOf(s)
    }

    protected void versionInDatabase(String s) {
        sql.firstRow(_ as String) >> new GroovyRowResult([version: s])
        updateContext.sourceVersion >> Version.valueOf(s)
    }
}
