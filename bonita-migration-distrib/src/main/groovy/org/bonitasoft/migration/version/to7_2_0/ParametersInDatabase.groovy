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
class ParametersInDatabase extends MigrationStep {


    @Override
    def execute(MigrationContext context) {
        def Map<Long, Long> parameterCount = [:]
        context.databaseHelper.executeScript("ParametersInDatabase", "")
        def engineServer = new File(context.bonitaHome, "engine-server")
        def work = new File(engineServer, "work")
        def tenants = new File(work, "tenants")
        tenants.eachFileMatch DIRECTORIES, ~/[0-9]+/, { tenant ->
            long tenantId = Long.valueOf(tenant.getName())
            parameterCount.put(tenantId, 1)
            def processesFile = new File(tenant, "processes")
            processesFile?.eachFileMatch DIRECTORIES, ~/[0-9]+/, { process ->
                long processId = Long.valueOf(process.getName())
                def parameters = new File(process, "parameters.properties")
                if (parameters.exists()) {
                    def properties = new Properties()
                    parameters.withInputStream { inputStream ->
                        properties.load(inputStream)
                    }
                    context.logger.info("Putting ${properties.size()} parameters in database for process $processId")
                    properties.each { parameter ->
                        def long parameterId = parameterCount.get(tenantId)
                        parameterCount.put(tenantId, parameterId + 1)
                        context.sql.executeInsert("INSERT INTO proc_parameter VALUES ($tenantId,$parameterId,$processId,${parameter.getKey()},${parameter.getValue()})")
                    }
                } else {
                    context.logger.warn("No parameter file for process $processId")
                }
            }
        }
        insertSequences(parameterCount, context)

    }

    private static Map<Long, Long> insertSequences(LinkedHashMap<Long, Long> parameterCount, context) {
        return parameterCount.each { it ->
            context.sql.executeInsert("INSERT INTO sequence VALUES(${it.getKey()}, ${10400}, ${it.getValue()})")
        }
    }


    @Override
    String getDescription() {
        return "Put parameters in database"
    }

}
