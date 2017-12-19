package org.bonitasoft.migration.version.to7_6_0

import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

/**
 * @author Emmanuel Duchastenier
 */
class RemoveDefaultGroovyScriptsAndUpdateTheirConfigurationTest extends Specification {

    RemoveDefaultGroovyScriptsAndUpdateTheirConfiguration migrationStep = new RemoveDefaultGroovyScriptsAndUpdateTheirConfiguration()

    @Unroll
    def "should #scriptName be considered custom: #isCustom"(String scriptName, boolean isCustom) {
        given:
        boolean custom = migrationStep.isCustomGroovyScript(scriptName)

        expect:
        custom == isCustom

        where:
        scriptName                                        | isCustom
        "MyCustomGroovyScript"                            | true
        "ActorMemberPermissionRule.groovy"                | false
        "ActorPermissionRule.groovy"                      | false
        "CaseContextPermissionRule.groovy"                | false
        "CasePermissionRule.groovy"                       | false
        "CaseVariablePermissionRule.groovy"               | false
        "CommentPermissionRule.groovy"                    | false
        "ConnectorInstancePermissionRule.groovy"          | false
        "DocumentPermissionRule.groovy"                   | false
        "ProcessConfigurationPermissionRule.groovy"       | false
        "ProcessConnectorDependencyPermissionRule.groovy" | false
        "ProcessInstantiationPermissionRule.groovy"       | false
        "ProcessPermissionRule.groovy"                    | false
        "ProcessResolutionProblemPermissionRule.groovy"   | false
        "ProcessSupervisorPermissionRule.groovy"          | false
        "ProfileEntryPermissionRule.groovy"               | false
        "ProfilePermissionRule.groovy"                    | false
        "TaskExecutionPermissionRule.groovy"              | false
        "TaskPermissionRule.groovy"                       | false
        "UserPermissionRule.groovy"                       | false
    }

    def "should script file with default content be considered unmodified"() {
        expect:
        migrationStep.isGroovyScriptFileUnchanged("ActorMemberPermissionRule.groovy", this.class.getResource("/to7_6_0/ActorMemberPermissionRule.groovy.txt").bytes)
    }

    def "should script file with default content be considered unmodified on Mac"() {
        given:
        def contentBytes = this.class.getResource("/to7_6_0/ActorMemberPermissionRule.groovy.txt").bytes
        def modifiedContent = new String(contentBytes, StandardCharsets.UTF_8).replaceAll("\r\n", "\r").replaceAll("\n", "\r")

        expect:
        migrationStep.isGroovyScriptFileUnchanged("ActorMemberPermissionRule.groovy", modifiedContent.bytes)
    }

    def "should script file with default content be considered unmodified on Windows"() {
        given:
        def contentBytes = this.class.getResource("/to7_6_0/ActorMemberPermissionRule.groovy.txt").bytes
        def modifiedContent = new String(contentBytes, StandardCharsets.UTF_8)
                .replaceAll("\r\n", "\n") // avoid doubling end of line in the next step
                .replaceAll("[\n|\r]", "\r\n")

        expect:
        migrationStep.isGroovyScriptFileUnchanged("ActorMemberPermissionRule.groovy", modifiedContent.bytes)
    }
}
