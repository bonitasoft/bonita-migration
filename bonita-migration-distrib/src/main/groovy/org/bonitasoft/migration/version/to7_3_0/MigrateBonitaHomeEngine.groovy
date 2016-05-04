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
        //put the default files
        insert("bonita-platform-init-community.properties", context, 0, "PLATFORM_INIT_ENGINE")
        insert("bonita-platform-community.properties", context, 0, "PLATFORM_ENGINE")
        insert("bonita-platform-custom.xml", context, 0, "PLATFORM_ENGINE")
        insert("bonita-platform-private-community.properties", context, 0, "PLATFORM_ENGINE")
        insert("bonita-tenant-community.properties", context, 0, "TENANT_TEMPLATE_ENGINE")
        insert("bonita-tenant-private-community.properties", context, 0, "TENANT_TEMPLATE_ENGINE")
        insert("bonita-tenants-custom.xml", context, 0, "TENANT_TEMPLATE_ENGINE")

        context.databaseHelper.allTenants.each {
            def tenantId = it.id as long
            insert("bonita-tenant-community.properties", context, tenantId, "TENANT_ENGINE")
            insert("bonita-tenant-private-community.properties", context, tenantId, "TENANT_ENGINE")
            insert("bonita-tenants-custom.xml", context, tenantId, "TENANT_ENGINE")
        }
        // LICENSES, PLATFORM_PORTAL, PLATFORM_INIT_ENGINE, PLATFORM_ENGINE, TENANT_PORTAL, TENANT_ENGINE, TENANT_TEMPLATE_ENGINE, TENANT_SECURITY_SCRIPTS, TENANT_TEMPLATE_SECURITY_SCRIPTS, TENANT_TEMPLATE_PORTAL
        return null
    }

    protected insert(String fileName, context, long tenantId, String type) {
        this.class.getResourceAsStream("/version/to_7_3_0/platform-resources/conf/" + fileName).withStream {
            context.sql.executeInsert("INSERT INTO configuration(tenant_id,content_type,resource_name,resource_content) VALUES (${tenantId}, ${type}, ${fileName}, ${it.bytes})");
        }
    }

    @Override
    String getDescription() {
        return "Migrate all engine data of the bonita home in database"
    }
}
