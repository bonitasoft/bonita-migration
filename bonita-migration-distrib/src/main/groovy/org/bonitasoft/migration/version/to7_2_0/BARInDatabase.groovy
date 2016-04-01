package org.bonitasoft.migration.version.to7_2_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

import static groovy.io.FileType.DIRECTORIES

/**
 *
 * parameters where in bonita-home/engine-server/work/tenants/<tenantId>/processes/<process_id>/parameters.properties
 *
 * sequence id: 10400
 * @author Baptiste Mesta
 */
class BARInDatabase extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        def Map<Long, Long> resourcesCount = [:]
        context.databaseHelper.executeScript("BARInDatabase", "")
        def engineServer = new File(context.bonitaHome, "engine-server")
        def work = new File(engineServer, "work")
        def tenants = new File(work, "tenants")
        tenants.eachFileMatch DIRECTORIES, ~/[0-9]+/, { tenant ->
            long tenantId = Long.valueOf(tenant.getName())
            resourcesCount.put(tenantId, 1)
            def processesFile = new File(tenant, "processes")
            if (processesFile.exists()) {
                processesFile.eachFileMatch DIRECTORIES, ~/[0-9]+/, { process ->
                    long processId = Long.valueOf(process.getName())
                    def nbDoc = migrateInitialDocument(context, resourcesCount, tenantId, process, processId)
                    def nbConnector = migrateConnectors(context, resourcesCount, tenantId, process, processId)
                    def nbUserfilter = migrateUserfilters(context, resourcesCount, tenantId, process, processId)
                    def nbexternal = migrateExternalResources(context, resourcesCount, tenantId, process, processId)
                    context.logger.info("put $nbDoc initial document, $nbConnector connector implementation, $nbUserfilter user filter implementation and $nbexternal external resources in database for process $processId")
                }
            }
        }
        insertSequences(resourcesCount, context)

    }

    def migrateConnectors(MigrationContext context, Map<Long, Long> resourcesCount, long tenantId, File process, long processId) {
        return addResourceRecursivly(new File(process, "connector"), context, processId, resourcesCount, tenantId, "", "CONNECTOR")

    }

    def migrateUserfilters(MigrationContext context, Map<Long, Long> resourcesCount, long tenantId, File process, long processId) {
        return addResourceRecursivly(new File(process, "userFilters"), context, processId, resourcesCount, tenantId, "", "USER_FILTER")
    }

    def migrateExternalResources(MigrationContext context, Map<Long, Long> resourcesCount, long tenantId, File process, long processId) {
        return addResourceRecursivly(new File(process, "resources"), context, processId, resourcesCount, tenantId, "", "EXTERNAL")
    }

    def migrateInitialDocument(MigrationContext context, Map<Long, Long> resourcesCount, long tenantId, File process, long processId) {
        return addResourceRecursivly(new File(process, "documents"), context, processId, resourcesCount, tenantId, "", "DOCUMENT")
    }

    private addResourceRecursivly(File folder, MigrationContext context, long processId, Map<Long, Long> resourcesCount, long tenantId, String parentPath, String type, int counter = 0) {
        if (folder.exists())
            folder.eachFile { resource ->
                if (resource.isFile()) {
                    context.logger.debug "put $type ${resource.name} of process $processId in resource table"
                    addResource(context, resourcesCount, tenantId, processId, parentPath, resource, type)
                    counter++
                } else {
                    addResourceRecursivly(resource, context, processId, resourcesCount, tenantId, parentPath + resource.getName() + "/", type, counter)
                }
            }
        counter
    }

    private void addResource(MigrationContext context, Map<Long, Long> resourcesCount, long tenantId, long processId, String parentPath, File file, String type) {
        def long resourceId = resourcesCount.get(tenantId)
        context.sql.executeInsert("INSERT INTO bar_resource VALUES ($tenantId,$resourceId,$processId,${parentPath + file.getName()},${type},${file.bytes})")
        resourcesCount.put(tenantId, resourceId + 1)
    }
    /*
    DOCUMENT, EXTERNAL, CONNECTOR, USER_FILTER

    tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  process_id INT8 NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(16) NOT NULL,
  content BYTEA NOT NULL,
     */

    private static Map<Long, Long> insertSequences(LinkedHashMap<Long, Long> resourcesCount, context) {
        return resourcesCount.each { it ->
            context.sql.executeInsert("INSERT INTO sequence VALUES(${it.getKey()}, ${10500}, ${it.getValue()})")
        }
    }


    @Override
    String getDescription() {
        return "Put Business archive in database"
    }

}
