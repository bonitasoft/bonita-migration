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
           //migrate tenant resources here
        }
        insertSequences(resourcesCount, context)

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
