package org.bonitasoft.migration.version.to7_6_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * @author Emmanuel Duchastenier
 */
class RemoveDefaultGroovyScriptsAndUpdateTheirConfiguration extends MigrationStep {

    public static final String DYNAMIC_PERMISSIONS_FILE = 'dynamic-permissions-checks-custom.properties'

    def LINE_SEPARATOR = System.lineSeparator()

    // sha1 sum of the groovy files in version 7.5.4:
    final Map checksumMap = ["ActorMemberPermissionRule.groovy"               : "0510a1aa823611442239058e2c5829aa92ecc532",
                             "ActorPermissionRule.groovy"                     : "0b457fb77caf7a66d1167e0e70fd1502f3b1adac",
                             "CaseContextPermissionRule.groovy"               : "762633a45c11f70a970344b176a5aa448a2e9ab5",
                             "CasePermissionRule.groovy"                      : "2626f1b435606eba6f7fc92922c3239f74965e96",
                             "CaseVariablePermissionRule.groovy"              : "c1aee4726352af3640f6c6dccb05047cbdbef3ad",
                             "CommentPermissionRule.groovy"                   : "7fb396db4c713a1643b34ac6d6b77d6867f6d9c8",
                             "ConnectorInstancePermissionRule.groovy"         : "5a350ac32bc0427f42f18c04050b15dd5d1a696a",
                             "DocumentPermissionRule.groovy"                  : "ab24186d10f98e7e3e760c810437ae83b9f3b3c8",
                             "ProcessConfigurationPermissionRule.groovy"      : "14f4c744d8c2d3f808295dc67fa1890882840505",
                             "ProcessConnectorDependencyPermissionRule.groovy": "7cf3d2e3f708d7d68af33e328eea6a8c179649a9",
                             "ProcessInstantiationPermissionRule.groovy"      : "348bbb4ecb73f31d2e9f9efbedf793ba672876f1",
                             "ProcessPermissionRule.groovy"                   : "970422ed0c3e0a8c2b041b1095710dbd2ea87e8f",
                             "ProcessResolutionProblemPermissionRule.groovy"  : "b9c8fb8e5f9b3ecd05f3af7fece6cac9baa75ddd",
                             "ProcessSupervisorPermissionRule.groovy"         : "bf52d8db2ba007c861d3bca1f6757c34c2136826",
                             "ProfileEntryPermissionRule.groovy"              : "8defb6d2d75ab04fd3d96d57277c0d8908773d6c",
                             "ProfilePermissionRule.groovy"                   : "a1e4cfd1136ac4355c1228475b19bf45de9c1b4f",
                             "TaskExecutionPermissionRule.groovy"             : "c226f62623233635d3b5593363bd4a9259256780",
                             "TaskPermissionRule.groovy"                      : "291162d21ef9a6ab9d7892b0dd00d2199fde44fe",
                             "UserPermissionRule.groovy"                      : "92d283255edced2a4e63313b8975b6cad1767cf8"]

    private String warnings = ''

    @Override
    execute(MigrationContext context) {
        migrateDynamicPermissionChecksCustom(context)
    }

    boolean isCustomGroovyScript(String scriptName) {
        !checksumMap.containsKey(scriptName)
    }

    boolean isGroovyScriptFileUnchanged(String scriptName, byte[] fileContent) {
        def reference = checksumMap.get(scriptName)

        MessageDigest sha1Digest = MessageDigest.getInstance("SHA1")
        byte[] sha1sum = sha1Digest.digest(new String(fileContent, StandardCharsets.UTF_8).normalize().bytes)
        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < sha1sum.length; i++) {
            sb.append(Integer.toString((sha1sum[i] & 0xff) + 0x100, 16).substring(1))
        }
        def sha1Hexa = sb.toString()
        !isCustomGroovyScript(scriptName) && reference == sha1Hexa
    }

    /**
     * For each rule used in file dynamic-permissions-checks-custom.properties:
     * - check if the used script is custom or default (and unmodified)
     * - if custom, leave it as is
     * - if default and unmodified, remove the groovy file from database, and update the configuration file with the new script package org.bonitasoft.permissions
     * - if default but modified, leave it as is and warn the user with a message on the good practices
     */
    def migrateDynamicPermissionChecksCustom(MigrationContext context) {
        // use a set to avoid duplicated warning for the same script on the same tenant:
        Set warns = new HashSet()
        context.sql.eachRow("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name = ?
                """, [DYNAMIC_PERMISSIONS_FILE]) {
            def tenantId = it.tenant_id as long
            def contentType = it.content_type as String
            def content = getBlobContent(it["resource_content"], context.databaseHelper.dbVendor)

            // for the current tenant, retrieve the groovy scripts and delete them if they have never been modified:
            List unmodifiedDefaultRules = []
            if (tenantId == 0) {
                purgeUnmodifiedScriptsFromDatabase(tenantId, unmodifiedDefaultRules, context, 'TENANT_TEMPLATE_SECURITY_SCRIPTS')
            } else {
                purgeUnmodifiedScriptsFromDatabase(tenantId, unmodifiedDefaultRules, context, 'TENANT_SECURITY_SCRIPTS')
            }

            def newContent = new ArrayList<String>()
            boolean contentIsUpdated = false
            content.eachLine { String line ->
                if (line ==~ /.*check\|.*]/) {
                    def ruleName = (line =~ /.*check\|(.*)]/)[0][1] as String
                    String groovyScriptName = "${ruleName}.groovy"
                    if (unmodifiedDefaultRules.contains(groovyScriptName)) {
                        // if the used script was unchanged (default), update the line with new package:
                        newContent.add(line.replace(ruleName, "org.bonitasoft.permissions.${ruleName}"))
                        contentIsUpdated = true
                    } else {
                        if (!isCustomGroovyScript(groovyScriptName)) {
                            warns.add """Warning, groovy script $groovyScriptName (from tenant $tenantId) is provided by Bonitasoft but has been
 modified since installation.${LINE_SEPARATOR}"""
                        }
                        // else, leave the line as is:
                        newContent.add(line)
                    }
                } else {
                    // if this is not a line defining a dynamic check using script:
                    newContent.add(line)
                }
            }
            if (contentIsUpdated) {
                context.configurationHelper.updateConfigurationFileContent("dynamic-permissions-checks-custom.properties", tenantId, contentType, newContent.join("\n").bytes)
            }
        }
        warns.forEach { warnings += it }
    }

    private purgeUnmodifiedScriptsFromDatabase(tenantId, List unmodifiedDefaultRules, MigrationContext context, String contentType) {
        context.sql.eachRow("""
                SELECT resource_name, resource_content, content_type
                FROM configuration
                WHERE tenant_id = ?
                AND content_type = ?
                """, [tenantId, contentType]) { row ->
            deleteScriptIfDefaultAndUnchanged(tenantId, context, row, unmodifiedDefaultRules)
        }
    }

    private void deleteScriptIfDefaultAndUnchanged(long tenantId, MigrationContext context, s, List unmodifiedDefaultRules) {
        def groovyScriptName = s.resource_name as String
        if (isGroovyScriptFileUnchanged(groovyScriptName, getBlobContent(s["resource_content"], context.databaseHelper.dbVendor).bytes)) {
            unmodifiedDefaultRules.add(groovyScriptName)
            // delete the file from the database:
            context.configurationHelper.deleteConfigurationFile(groovyScriptName, tenantId, s.content_type as String)
        }
    }

    def getBlobContent(Object blobValue, DBVendor dbVendor) {
        def bytesContent
        if (DBVendor.ORACLE == dbVendor) {
            bytesContent = blobValue.binaryStream
        } else {
            bytesContent = new ByteArrayInputStream(blobValue)
        }
        bytesContent
    }

    @Override
    String getDescription() {
        return "Migrate permission rule Groovy scripts and their configuration to the new mechanism"
    }

    @Override
    String getWarning() {
        if (warnings) {
            warnings += """For these files, you will not benefit from the potential fixes in the future.
${LINE_SEPARATOR}You are advised to customize your scripts by creating new ones in separate files.
${LINE_SEPARATOR}See https://documentation.bonitasoft.com/?page=rest-api-authorization for more details."""
        }
        warnings
    }

}
