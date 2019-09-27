package org.bonitasoft.migration.core


import spock.lang.Specification

class MigrationVerifierTest extends Specification {


    private def infos = []
    private def warns = []
    private def errors = []
    private def logger = [
            info : { String message -> infos.add(message) },
            warn : { String message -> warns.add(message) },
            error: { String message -> errors.add(message) }] as Logger
    private MigrationVerifier runner = new MigrationVerifier()


    def setup() {
        runner.logger = logger
    }


    def "should log blocking errors"() {
        given:
        runner.migrationVersions = [
                new VersionMigration() {
                    @Override
                    List<MigrationStep> getMigrationSteps() { [] }

                    @Override
                    String[] getPreMigrationBlockingMessages(MigrationContext context) {
                        return ["blocking message 1", "blocking message 2"]
                    }
                }.with {
                    version = "1.2.0"
                    it
                },
                new VersionMigration() {
                    @Override
                    List<MigrationStep> getMigrationSteps() { [] }

                    @Override
                    String[] getPreMigrationBlockingMessages(MigrationContext context) {
                        return ["blocking message 1"]
                    }
                }.with {
                    version = "1.2.1"
                    it
                }]

        when:
        runner.run(false)
        then:
        errors == [
                "Migration to version 1.2.1 is currently not possible:",
                " * Step 1.2.0:",
                "blocking message 1",
                "blocking message 2",
                " * Step 1.2.1:",
                "blocking message 1"
        ]
    }


    def "should log last possible version if applicable"() {
        given:
        runner.migrationVersions = [
                new VersionMigration() {
                    @Override
                    List<MigrationStep> getMigrationSteps() { [] }
                }.with {
                    version = "1.2.0"
                    it
                },
                new VersionMigration() {
                    @Override
                    List<MigrationStep> getMigrationSteps() { [] }

                    @Override
                    String[] getPreMigrationBlockingMessages(MigrationContext context) {
                        return ["blocking message 1"]
                    }
                }.with {
                    version = "1.2.1"
                    it
                }]

        when:
        runner.run(false)
        then:
        errors == [
                "Migration to version 1.2.1 is currently not possible, you can only migrate to version 1.2.0:",
                " * Step 1.2.1:",
                "blocking message 1"
        ]
    }

    def "should log warnings"() {
        given:
        runner.migrationVersions = [
                new VersionMigration() {
                    @Override
                    List<MigrationStep> getMigrationSteps() { [] }

                    @Override
                    String[] getPreMigrationWarnings(MigrationContext context) {
                        return ["warning message 1", "warning message 2"]
                    }
                }.with {
                    version = "1.2.0"
                    it
                },
                new VersionMigration() {
                    @Override
                    List<MigrationStep> getMigrationSteps() { [] }

                    @Override
                    String[] getPreMigrationWarnings(MigrationContext context) {
                        return ["warning message 1"]
                    }
                }.with {
                    version = "1.2.1"
                    it
                }]

        when:
        runner.run(false)
        then:
        infos.empty
        errors.empty
        warns == [
                "Migration to version 1.2.1 is possible but there is some warnings:",
                " * Step 1.2.0:",
                "warning message 1",
                "warning message 2",
                " * Step 1.2.1:",
                "warning message 1"]
    }


    def "should log that migration is possible"() {
        given:
        runner.migrationVersions = [
                new VersionMigration() {
                    @Override
                    List<MigrationStep> getMigrationSteps() { [] }
                }.with {
                    version = "1.2.0"
                    it
                },
                new VersionMigration() {
                    @Override
                    List<MigrationStep> getMigrationSteps() { [] }
                }.with {
                    version = "1.2.1"
                    it
                }]

        when:
        runner.run(false)
        then:
        infos == ["Migration to version 1.2.1 is possible."]
        errors.empty
        warns.empty
    }
}
