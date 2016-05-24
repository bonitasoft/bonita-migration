package org.bonitasoft.migration.version.to7_3_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Baptiste Mesta
 */
class MigrateBonitaHomeEngine extends MigrationStep {
    @Override
    def execute(MigrationContext context) {
        //create table configuration
        context.databaseHelper.executeScript("configuration", "")
        context.logger.info("insert configuration files")

        //put the default files
        insert(context, "bonita-platform-init-community.properties", 0, "PLATFORM_INIT_ENGINE")
        insert(context, "bonita-platform-community.properties", 0, "PLATFORM_ENGINE")
        insert(context, "bonita-platform-custom.xml", 0, "PLATFORM_ENGINE")
        insert(context, "bonita-platform-private-community.properties", 0, "PLATFORM_ENGINE")
        insert(context, "bonita-tenant-community.properties", 0, "TENANT_TEMPLATE_ENGINE")
        insert(context, "bonita-tenant-private-community.properties", 0, "TENANT_TEMPLATE_ENGINE")
        insert(context, "bonita-tenants-custom.xml", 0, "TENANT_TEMPLATE_ENGINE")

        context.databaseHelper.allTenants.each {
            def tenantId = it.id as long
            insert(context, "bonita-tenant-community.properties", tenantId, "TENANT_ENGINE")
            insert(context, "bonita-tenant-private-community.properties", tenantId, "TENANT_ENGINE")
            insert(context, "bonita-tenants-custom.xml", tenantId, "TENANT_ENGINE")
        }
        // LICENSES, PLATFORM_PORTAL, PLATFORM_INIT_ENGINE, PLATFORM_ENGINE, TENANT_PORTAL, TENANT_ENGINE, TENANT_TEMPLATE_ENGINE, TENANT_SECURITY_SCRIPTS, TENANT_TEMPLATE_SECURITY_SCRIPTS, TENANT_TEMPLATE_PORTAL
        return null
    }

    protected insert(MigrationContext context, String fileName, long tenantId, String type) {
        this.class.getResourceAsStream("/version/to_7_3_0/platform-resources/conf/" + fileName).withStream {
            context.databaseHelper.insertConfigurationFile(fileName, tenantId, type, it.bytes)
        }
    }

    @Override
    String getDescription() {
        return "Migrate all engine data of the bonita home in database"
    }
}
