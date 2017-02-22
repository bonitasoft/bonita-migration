package org.bonitasoft.migration.version.to7_5_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Emmanuel Duchastenier
 */
class SplitRestSecurityConfig extends MigrationStep {

    @Override
    execute(MigrationContext context) {
        migrateResourcePermissionMappings(context)
        migrateCompoundPermissionMappings(context)
        migrateDynamicPermissionChecks(context)
    }

    def migrateDynamicPermissionChecks(MigrationContext context) {
        // For all tenants and tenant-template, rename current file with '-custom':
        context.databaseHelper.sql.execute("UPDATE configuration SET resource_name='dynamic-permissions-checks-custom.properties' WHERE resource_name='dynamic-permissions-checks.properties'")

        def defaultProperties = this.getClass().getResourceAsStream("/version/to_7_5_0/dynamic-permissions-checks-default.properties").bytes

        // For each tenant...
        context.sql.eachRow("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name='dynamic-permissions-checks-custom.properties'
                """) {
            // ... insert new version of file 'dynamic-permissions-checks.properties' with default commented values:
            insert(context, 'dynamic-permissions-checks.properties', it.tenant_id as long, it.content_type as String, defaultProperties)
        }
    }

    def migrateResourcePermissionMappings(MigrationContext context) {
        // For each file 'resources-permissions-mapping.properties' in database (one per tenant + one for tenant-template):
        // Compare each line to the lines in the default provided file and split the default provided values => default file resources-permissions-mapping.properties
        // with the custom lines (not present in the default file) => custom file resources-permissions-mapping-custom.properties
        // Also, add an empty 'resources-permissions-mapping-internal.properties' file.
        migratePermissionFile(context, 'resources-permissions-mapping')
    }

    def migrateCompoundPermissionMappings(MigrationContext context) {
        // For each file 'compound-permissions-mapping.properties' in database (one per tenant + one for tenant-template):
        // Compare each line to the lines in the default provided file and split the default provided values => default file compound-permissions-mapping.properties
        // with the custom lines (not present in the default file) => custom file compound-permissions-mapping-custom.properties
        // Also, add an empty 'compound-permissions-mapping-internal.properties' file.
        migratePermissionFile(context, 'compound-permissions-mapping')
    }

    def migratePermissionFile(MigrationContext context, String permissionRawFilename) {
        def filename = "${permissionRawFilename}.properties" as String
        context.sql.eachRow("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name = ?
                """, [filename]) {
            def tenantId = it.tenant_id as long
            def contentType = it.content_type as String
            def content
            if (DBVendor.ORACLE == context.dbVendor) {
                content = (it["resource_content"]).binaryStream
            } else {
                content = new ByteArrayInputStream(it["resource_content"])
            }

            def defaultProperties = this.getClass().getResourceAsStream("/version/to_7_5_0/${permissionRawFilename}-default.properties").readLines("UTF-8")
            def newDefaultProperties = new ArrayList<String>();
            def customProperties = this.getClass().getResourceAsStream("/version/to_7_5_0/${permissionRawFilename}-custom.properties").readLines("UTF-8")

            content.eachLine { String line ->
                if (defaultProperties.contains(line)) {
                    // add the line in default file:
                    newDefaultProperties.add(line)
                } else {
                    // add the line in custom file:
                    customProperties.add(line)
                }
            }
            update(context, "${permissionRawFilename}.properties", tenantId, contentType, newDefaultProperties.join("\n").bytes)
            insert(context, "${permissionRawFilename}-custom.properties", tenantId, contentType, customProperties.join("\n").bytes)
            insert(context, "${permissionRawFilename}-internal.properties", tenantId, contentType, '# for internal use only'.bytes)
        }
    }


    protected insert(MigrationContext context, String fileName, long tenantId, String type, byte[] content) {
        context.databaseHelper.insertConfigurationFile(fileName, tenantId, type, content)
    }

    protected update(MigrationContext context, String fileName, long tenantId, String type, byte[] content) {
        context.databaseHelper.updateConfigurationFileContent(fileName, tenantId, type, content)
    }

    @Override
    String getDescription() {
        return "Split REST security configuration files"
    }

}
