package org.bonitasoft.migration.version.to7_3_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Baptiste Mesta
 */
class MigrateBonitaHome extends MigrationStep {
    @Override
    def execute(MigrationContext context) {
        //create table configuration
        context.databaseHelper.executeScript("configuration", "")
        context.logger.info("insert configuration files")
        //put the default files
        insert(context, "platform_init_engine/bonita-platform-init-community-custom.properties", 0, "PLATFORM_INIT_ENGINE")
        insert(context, "platform_init_engine/bonita-platform-init-custom.xml", 0, "PLATFORM_INIT_ENGINE")

        insert(context, "platform_engine/bonita-platform-community-custom.properties", 0, "PLATFORM_ENGINE")
        insert(context, "platform_engine/bonita-platform-custom.xml", 0, "PLATFORM_ENGINE")

        insert(context, "tenant_template_engine/bonita-tenant-community-custom.properties", 0, "TENANT_TEMPLATE_ENGINE")
        insert(context, "tenant_template_engine/bonita-tenants-custom.xml", 0, "TENANT_TEMPLATE_ENGINE")

        insert(context, "platform_portal/cache-config.xml", 0, "PLATFORM_PORTAL")
        insert(context, "platform_portal/jaas-standard.cfg", 0, "PLATFORM_PORTAL")
        insert(context, "platform_portal/platform-tenant-config.properties", 0, "PLATFORM_PORTAL")
        insert(context, "platform_portal/security-config.properties", 0, "PLATFORM_PORTAL")

        insert(context, "tenant_template_portal/authenticationManager-config.properties", 0, "TENANT_TEMPLATE_PORTAL")
        insert(context, "tenant_template_portal/autologin-v6.json", 0, "TENANT_TEMPLATE_PORTAL")
        insert(context, "tenant_template_portal/compound-permissions-mapping.properties", 0, "TENANT_TEMPLATE_PORTAL")
        insert(context, "tenant_template_portal/console-config.properties", 0, "TENANT_TEMPLATE_PORTAL")
        insert(context, "tenant_template_portal/custom-permissions-mapping.properties", 0, "TENANT_TEMPLATE_PORTAL")
        insert(context, "tenant_template_portal/dynamic-permissions-checks.properties", 0, "TENANT_TEMPLATE_PORTAL")
        insert(context, "tenant_template_portal/forms-config.properties", 0, "TENANT_TEMPLATE_PORTAL")
        insert(context, "tenant_template_portal/resources-permissions-mapping.properties", 0, "TENANT_TEMPLATE_PORTAL")
        insert(context, "tenant_template_portal/security-config.properties", 0, "TENANT_TEMPLATE_PORTAL")

        insert(context, "tenant_template_security_scripts/ActorMemberPermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/ActorPermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/CaseContextPermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/CasePermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/CaseVariablePermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/CommentPermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/ConnectorInstancePermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/DocumentPermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/ProcessConfigurationPermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/ProcessConnectorDependencyPermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/ProcessInstantiationPermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/ProcessPermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/ProcessResolutionProblemPermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/ProcessSupervisorPermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/ProfileEntryPermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/ProfilePermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/TaskExecutionPermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/TaskPermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")
        insert(context, "tenant_template_security_scripts/UserPermissionRule.groovy", 0, "TENANT_TEMPLATE_SECURITY_SCRIPTS")

        context.databaseHelper.allTenants.each {
            def tenantId = it.id as long
            insert(context, "tenant_template_engine/bonita-tenant-community-custom.properties", tenantId, "TENANT_ENGINE")
            insert(context, "tenant_template_engine/bonita-tenants-custom.xml", tenantId, "TENANT_ENGINE")


            insert(context, "tenant_template_portal/authenticationManager-config.properties", tenantId, "TENANT_PORTAL")
            insert(context, "tenant_template_portal/autologin-v6.json", tenantId, "TENANT_PORTAL")
            insert(context, "tenant_template_portal/compound-permissions-mapping.properties", tenantId, "TENANT_PORTAL")
            insert(context, "tenant_template_portal/console-config.properties", tenantId, "TENANT_PORTAL")
            insert(context, "tenant_template_portal/custom-permissions-mapping.properties", tenantId, "TENANT_PORTAL")
            insert(context, "tenant_template_portal/dynamic-permissions-checks.properties", tenantId, "TENANT_PORTAL")
            insert(context, "tenant_template_portal/forms-config.properties", tenantId, "TENANT_PORTAL")
            insert(context, "tenant_template_portal/resources-permissions-mapping.properties", tenantId, "TENANT_PORTAL")
            insert(context, "tenant_template_portal/security-config.properties", tenantId, "TENANT_PORTAL")

            insert(context, "tenant_template_security_scripts/ActorMemberPermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/ActorPermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/CaseContextPermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/CasePermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/CaseVariablePermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/CommentPermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/ConnectorInstancePermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/DocumentPermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/ProcessConfigurationPermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/ProcessConnectorDependencyPermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/ProcessInstantiationPermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/ProcessPermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/ProcessResolutionProblemPermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/ProcessSupervisorPermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/ProfileEntryPermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/ProfilePermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/TaskExecutionPermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/TaskPermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
            insert(context, "tenant_template_security_scripts/UserPermissionRule.groovy", tenantId, "TENANT_SECURITY_SCRIPTS")
        }
        return null
    }


    protected insert(MigrationContext context, String fileName, long tenantId, String type) {
        this.class.getResourceAsStream("/version/to_7_3_0/platform-resources/" + fileName).withStream {
            context.configurationHelper.insertConfigurationFile(fileName.split('/')[1], tenantId, type, it.bytes)
        }
    }

    @Override
    String getDescription() {
        return "Migrate all engine data of the bonita home in database"
    }
}
