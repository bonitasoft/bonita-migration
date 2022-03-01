package org.bonitasoft.update.core


import spock.lang.Specification

class UpdateVerifierTest extends Specification {


    private def infos = []
    private def warns = []
    private def errors = []
    private def logger = [
            info : { String message -> infos.add(message) },
            warn : { String message -> warns.add(message) },
            error: { String message -> errors.add(message) }] as Logger
    private UpdateVerifier runner = new UpdateVerifier()


    def setup() {
        runner.logger = logger
    }


    def "should log blocking errors"() {
        given:
        runner.versionUpdates = [
                new VersionUpdate() {
                    @Override
                    List<UpdateStep> getUpdateSteps() { [] }

                    @Override
                    String[] getPreUpdateBlockingMessages(UpdateContext context) {
                        return ["blocking message 1", "blocking message 2"]
                    }
                }.with {
                    version = "1.2.0"
                    it
                },
                new VersionUpdate() {
                    @Override
                    List<UpdateStep> getUpdateSteps() { [] }

                    @Override
                    String[] getPreUpdateBlockingMessages(UpdateContext context) {
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
                "Update to version 1.2.1 is currently not possible:",
                " * Step 1.2.0:",
                "blocking message 1",
                "blocking message 2",
                " * Step 1.2.1:",
                "blocking message 1"
        ]
    }


    def "should log last possible version if applicable"() {
        given:
        runner.versionUpdates = [
                new VersionUpdate() {
                    @Override
                    List<UpdateStep> getUpdateSteps() { [] }
                }.with {
                    version = "1.2.0"
                    it
                },
                new VersionUpdate() {
                    @Override
                    List<UpdateStep> getUpdateSteps() { [] }

                    @Override
                    String[] getPreUpdateBlockingMessages(UpdateContext context) {
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
                "Update to version 1.2.1 is currently not possible, you can only update to version 1.2.0:",
                " * Step 1.2.1:",
                "blocking message 1"
        ]
    }

    def "should log warnings"() {
        given:
        runner.versionUpdates = [
                new VersionUpdate() {
                    @Override
                    List<UpdateStep> getUpdateSteps() { [] }

                    @Override
                    String[] getPreUpdateWarnings(UpdateContext context) {
                        return ["warning message 1", "warning message 2"]
                    }
                }.with {
                    version = "1.2.0"
                    it
                },
                new VersionUpdate() {
                    @Override
                    List<UpdateStep> getUpdateSteps() { [] }

                    @Override
                    String[] getPreUpdateWarnings(UpdateContext context) {
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
                "Update to version 1.2.1 is possible but there is some warnings:",
                " * Step 1.2.0:",
                "warning message 1",
                "warning message 2",
                " * Step 1.2.1:",
                "warning message 1"]
    }


    def "should log that update is possible"() {
        given:
        runner.versionUpdates = [
                new VersionUpdate() {
                    @Override
                    List<UpdateStep> getUpdateSteps() { [] }
                }.with {
                    version = "1.2.0"
                    it
                },
                new VersionUpdate() {
                    @Override
                    List<UpdateStep> getUpdateSteps() { [] }
                }.with {
                    version = "1.2.1"
                    it
                }]

        when:
        runner.run(false)
        then:
        infos == ["Update to version 1.2.1 is possible."]
        errors.empty
        warns.empty
    }
}
