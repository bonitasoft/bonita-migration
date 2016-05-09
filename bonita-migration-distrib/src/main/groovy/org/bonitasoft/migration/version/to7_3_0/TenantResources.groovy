package org.bonitasoft.migration.version.to7_3_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

import static groovy.io.FileType.DIRECTORIES

/**
 *
 * parameters where in bonita-home/engine-server/work/tenants/<tenantId>/processes/<process_id>/parameters.properties
 *
 * sequence id: 10501
 * @author Baptiste Mesta
 */
class TenantResources extends MigrationStep {


    @Override
    def execute(MigrationContext context) {
        def Map<Long, Long> resourcesCount = [:]
        context.databaseHelper.executeScript("TenantResources", "")
        def engineServer = new File(context.bonitaHome, "engine-server")
        def work = new File(engineServer, "work")
        def tenants = new File(work, "tenants")
        tenants.eachFileMatch DIRECTORIES, ~/[0-9]+/, { tenant ->
            long tenantId = Long.valueOf(tenant.getName())
            resourcesCount.put(tenantId, 1)
            migrateClientBDMZip(context, resourcesCount, tenant, tenantId)
        }
        insertSequences(resourcesCount, context)

    }

    void migrateClientBDMZip(MigrationContext context, Map<Long, Long> resourcesCount, File tenantFolder, long tenantId) {
        def clientBDMZip = new File(new File(tenantFolder, "data-management-client"), "client-bdm.zip")
        if (!clientBDMZip.exists()) {
            return
        }
        def long resourceId = resourcesCount.get(tenantId)
        context.logger.info("Put client bdm zip in database with id $resourceId for tenant $tenantId")
        context.sql.executeInsert("INSERT INTO tenant_resource VALUES ($tenantId,$resourceId,${"client-bdm.zip"},${"BDM"},${clientBDMZip.bytes})")
        clientBDMZip.delete()
        resourcesCount.put(tenantId, resourceId + 1)
    }

    private static Map<Long, Long> insertSequences(LinkedHashMap<Long, Long> resourcesCount, context) {
        return resourcesCount.each { it ->
            context.sql.executeInsert("INSERT INTO sequence VALUES(${it.getKey()}, ${10501}, ${it.getValue()})")
        }
    }


    @Override
    String getDescription() {
        return "Add tenant resources in database"
    }

}
