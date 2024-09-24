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
                List<UpdateStep> getUpdateSteps() {
                    []
                }

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
                List<UpdateStep> getUpdateSteps() {
                    []
                }

                @Override
                String[] getPreUpdateBlockingMessages(UpdateContext context) {
                    return ["blocking message 1"]
                }
            }.with {
                version = "1.2.1"
                it
            }
        ]

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
                List<UpdateStep> getUpdateSteps() {
                    []
                }
            }.with {
                version = "1.2.0"
                it
            },
            new VersionUpdate() {
                @Override
                List<UpdateStep> getUpdateSteps() {
                    []
                }

                @Override
                String[] getPreUpdateBlockingMessages(UpdateContext context) {
                    return ["blocking message 1"]
                }
            }.with {
                version = "1.2.1"
                it
            }
        ]

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
                List<UpdateStep> getUpdateSteps() {
                    []
                }

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
                List<UpdateStep> getUpdateSteps() {
                    []
                }

                @Override
                String[] getPreUpdateWarnings(UpdateContext context) {
                    return ["warning message 1"]
                }
            }.with {
                version = "1.2.1"
                it
            }
        ]

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
            "warning message 1"
        ]
    }


    def "should log that update is possible"() {
        given:
        runner.versionUpdates = [
            new VersionUpdate() {
                @Override
                List<UpdateStep> getUpdateSteps() {
                    []
                }
            }.with {
                version = "1.2.0"
                it
            },
            new VersionUpdate() {
                @Override
                List<UpdateStep> getUpdateSteps() {
                    []
                }
            }.with {
                version = "1.2.1"
                it
            }
        ]

        when:
        runner.run(false)
        then:
        infos == ["Update to version 1.2.1 is possible."]
        errors.empty
        warns.empty
    }
}
