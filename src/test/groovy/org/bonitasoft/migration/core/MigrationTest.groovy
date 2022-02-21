package org.bonitasoft.migration.core

import com.github.zafarkhaja.semver.Version
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files

/**
 * @author Baptiste Mesta
 */
class MigrationTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    private def infos = []
    private def logger = [info: { String message -> infos.add(message) }] as Logger
    private Sql sql = Mock(Sql)
    private MigrationContext migrationContext = Mock(MigrationContext)
    private DisplayUtil displayUtil = Mock(DisplayUtil)
    private Migration migration
    private MigrationRunner runner = Mock(MigrationRunner)

    def setup() {
        migrationContext.sql >> sql
        migrationContext.logger >> logger
        migration = Spy(Migration, constructorArgs: [migrationContext, displayUtil])
        migration.createRunner() >> runner
    }

    def "migrate with an unhandled source version should throw exception"() {
        given:
        versionInDatabase("7.9.0")
        targetVersion("7.11.0")
        when:
        migration.run(false)
        then:
        IllegalStateException throwable = thrown()
        throwable.message == "Sorry, but this tool can't manage version before 7.10.0, use the migration tool version 2"
    }

    def "migrate with an unhandled target version should throw exception"() {
        given:
        versionInDatabase("7.11.0")
        targetVersion("7.25.0")
        when:
        migration.run(false)
        then:
        IllegalStateException throwable = thrown()
        throwable.message == "7.25.0 is not yet handled by this version of the migration tool"
    }

    def "migrate with a version before 7.0 should throw exception"() {
        given:
        versionInDatabase("6.5.0")
        targetVersion("7.11.0")
        when:
        migration.run(false)
        then:
        IllegalStateException throwable = thrown()
        throwable.message == "Sorry, but this tool can't manage version before 7.10.0, use the migration tool version 2"
    }

    def "same target and source version should throw exception"() {
        given:
        versionInDatabase("7.11.0")
        targetVersion("7.11.0")
        when:
        migration.run(false)
        then:
        IllegalStateException throwable = thrown()
        throwable.message == "The version is already in 7.11"
    }


    def "target before source version should throw exception"() {
        given:
        versionInDatabase("7.12.0")
        targetVersion("7.11.0")
        when:
        migration.run(false)
        then:
        IllegalStateException throwable = thrown()
        throwable.message == "The target version 7.11.0 can not be before source version 7.12"
    }

    @Unroll
    def "should migration from #source to #target execute steps #migrationVersions"() {
        given:
        versionInDatabase(source)
        targetVersion(target)
        when:
        migration.run(false)
        then:
        1 * runner.setProperty('migrationVersions', ({
            it.collect { it.class.name.split('\\.').last() } == migrationVersions
        }))

        where:
        source  | target  || migrationVersions
        "7.10.0" | "7.11.0" || ["MigrateTo7_11_0"]
        "7.10.2" | "7.12.0" || ["MigrateTo7_11_0", "MigrateTo7_12_0"]
        "7.10.3" | "7.13.0" || ["MigrateTo7_11_0", "MigrateTo7_12_0", "MigrateTo7_13_0"]
    }

    //There are no "forbidden" version right now
//    def "should NOT throw exception if trying to migrate to 7.7.0 and SysProp \"ignore.invalid.target.version\" provided"() {
//        given:
//        versionInDatabase("7.6.3")
//        targetVersion("7.7.0")
//        System.setProperty("ignore.invalid.target.version", "value")
//
//        when:
//        migration.run(false)
//
//        then:
//        noExceptionThrown()
//
//        cleanup:
//        System.clearProperty("ignore.invalid.target.version")
//    }
//
//    def "should INDEED throw exception if trying to migrate to 7.6.9 and SysProp \"ignore.invalid.target.version\" provided but 7.6.9 not in invisible TRANSITION VERSION list"() {
//        given:
//        versionInDatabase("7.6.3")
//        targetVersion("7.6.9")
//        System.setProperty("ignore.invalid.target.version", "value")
//
//        when:
//        migration.run(false)
//
//        then:
//        IllegalStateException throwable = thrown()
//        throwable.message == "7.6.9 is not yet handled by this version of the migration tool"
//
//        cleanup:
//        System.clearProperty("ignore.invalid.target.version")
//    }

    def "should allow to migrate between 2 version handled by the tool"() {
        given:
        versionInDatabase("7.11.0")
        targetVersion("7.12.0")

        when:
        migration.run(true)

        then:
        noExceptionThrown()
    }


    protected void targetVersion(String s) {
        migrationContext.targetVersion >> Version.valueOf(s)
    }

    protected void versionInDatabase(String s) {
        sql.firstRow(_ as String) >> new GroovyRowResult([version: s])
        migrationContext.sourceVersion >> Version.valueOf(s)
    }

}
