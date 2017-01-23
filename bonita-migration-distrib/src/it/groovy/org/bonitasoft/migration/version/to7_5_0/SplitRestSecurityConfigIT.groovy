package org.bonitasoft.migration.version.to7_5_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Emmanuel Duchastenier
 */
class SplitRestSecurityConfigIT extends Specification {

    @Shared
    Logger logger = Mock(Logger)

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def setup() {
        dropTestTables()
        migrationContext.setVersion("7.5.0")
        dbUnitHelper.createTables("7_5_0/rest_secu_config")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["configuration"] as String[])
    }

    def "should migrate resources-permissions-mapping.properties"() {
        given:
        def currentFileContent = "# Identity resources\nGET|identity/user=[organization_visualization]\nGET|acme/business=[acme_permission]"

        migrationContext.sql.execute("""
            INSERT INTO configuration (tenant_id, content_type, resource_name, resource_content)
            VALUES (1, 'TENANT_PORTAL', 'resources-permissions-mapping.properties', $currentFileContent.bytes)
            """)

        when:
        new SplitRestSecurityConfig().execute(migrationContext)

        def defaultProps = dbUnitHelper.getBlobContentAsString(migrationContext.sql.firstRow("""SELECT resource_content FROM configuration WHERE tenant_id = 1 and content_type = 'TENANT_PORTAL'
                AND resource_name = 'resources-permissions-mapping.properties'""")["resource_content"])

        def customProps = dbUnitHelper.getBlobContentAsString(migrationContext.sql.firstRow("""SELECT resource_content FROM configuration WHERE tenant_id = 1 and content_type = 'TENANT_PORTAL'
                AND resource_name = 'resources-permissions-mapping-custom.properties'""")["resource_content"])
        def expectedCustomProps = this.getClass().getResourceAsStream("/version/to_7_5_0/resources-permissions-mapping-custom.properties").text.concat('\nGET|acme/business=[acme_permission]')

        def internalPropFile = dbUnitHelper.getBlobContentAsString(migrationContext.sql.firstRow("""SELECT resource_content FROM configuration WHERE tenant_id = 1 and content_type = 'TENANT_PORTAL'
                AND resource_name = 'resources-permissions-mapping-internal.properties'""")["resource_content"])

        then:

        defaultProps == '# Identity resources\nGET|identity/user=[organization_visualization]'
        customProps == expectedCustomProps
        internalPropFile == '# for internal use only'
    }

    def "should migrate compound-permissions-mapping.properties"() {
        given:
        def currentFileContent = "# List of permissions used for each pages.\ntenantMaintenance=[tenant_platform_management, tenant_platform_visualization, download_document, avatars]\nmyCustomCompound=[some_permission, some_other_permission]"

        migrationContext.sql.execute("""
            INSERT INTO configuration (tenant_id, content_type, resource_name, resource_content)
            VALUES (1, 'TENANT_PORTAL', 'compound-permissions-mapping.properties', $currentFileContent.bytes)
            """)

        when:
        new SplitRestSecurityConfig().execute(migrationContext)

        def defaultProps = dbUnitHelper.getBlobContentAsString(migrationContext.sql.firstRow("""SELECT resource_content FROM configuration WHERE tenant_id = 1 and content_type = 'TENANT_PORTAL'
                AND resource_name = 'compound-permissions-mapping.properties'""")["resource_content"])

        def customProps = dbUnitHelper.getBlobContentAsString(migrationContext.sql.firstRow("""SELECT resource_content FROM configuration WHERE tenant_id = 1 and content_type = 'TENANT_PORTAL'
                AND resource_name = 'compound-permissions-mapping-custom.properties'""")["resource_content"])
        def expectedCustomProps = this.getClass().getResourceAsStream("/version/to_7_5_0/compound-permissions-mapping-custom.properties").text.concat('\nmyCustomCompound=[some_permission, some_other_permission]')

        def internalPropFile = dbUnitHelper.getBlobContentAsString(migrationContext.sql.firstRow("""SELECT resource_content FROM configuration WHERE tenant_id = 1 and content_type = 'TENANT_PORTAL'
                AND resource_name = 'compound-permissions-mapping-internal.properties'""")["resource_content"])

        then:

        defaultProps == "# List of permissions used for each pages.\ntenantMaintenance=[tenant_platform_management, tenant_platform_visualization, download_document, avatars]"
        customProps == expectedCustomProps
        internalPropFile == '# for internal use only'
    }

    def "should migrate dynamic-permissions-checks.properties"() {
        given:
        def currentFileContent = "# uncommented line below\nGET|bpm/actor=[profile|Administrator, check|ActorPermissionRule]"

        migrationContext.sql.execute("""
            INSERT INTO configuration (tenant_id, content_type, resource_name, resource_content)
            VALUES (2, 'TENANT_TEMPLATE_PORTAL', 'dynamic-permissions-checks.properties', $currentFileContent.bytes)
            """)

        when:
        new SplitRestSecurityConfig().execute(migrationContext)

        def defaultProps = dbUnitHelper.getBlobContentAsString(migrationContext.sql.firstRow("""SELECT resource_content FROM configuration WHERE tenant_id = 2 and content_type = 'TENANT_TEMPLATE_PORTAL'
                AND resource_name = 'dynamic-permissions-checks.properties'""")["resource_content"])

        def customProps = dbUnitHelper.getBlobContentAsString(migrationContext.sql.firstRow("""SELECT resource_content FROM configuration WHERE tenant_id = 2 and content_type = 'TENANT_TEMPLATE_PORTAL'
                AND resource_name = 'dynamic-permissions-checks-custom.properties'""")["resource_content"])

        then:
        defaultProps.contains("Do not Edit this file manually")
        customProps == currentFileContent
    }
}
