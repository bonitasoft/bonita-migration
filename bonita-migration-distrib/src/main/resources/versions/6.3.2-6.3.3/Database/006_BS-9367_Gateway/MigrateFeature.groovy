import org.bonitasoft.migration.core.MigrationUtil;
import org.bonitasoft.migration.core.IOUtil;


def tenantsId = MigrationUtil.getTenantsId(dbVendor, sql)

IOUtil.executeWrappedWithTabs {
    println "Executing update for each tenant : " + tenantsId
    tenantsId.each {
        println "For tenant with id = " + it
        IOUtil.executeWrappedWithTabs {
            println "Getting Failed gateways"
            def parameters = Collections.singletonMap(":tenantId", String.valueOf(it))
            def List<Long> failedGatewaysId = MigrationUtil.getIds(feature, dbVendor, "get_failed_gateway_instances_id", parameters, sql)
            println "Found " + failedGateways.size() + " gateways"
            
            println "Getting related Archived gateway instances"
            for (Long failedGatewayId : failedGatewaysId) {
                // Get the gateways in unknown state
                parameters = new HashMap()
                parameters.put(":tenantId", String.valueOf(it))
                parameters.put(":flowNodeInstanceId", String.valueOf(failedGatewayId))
                def List<Long> archGatesInUnknownState = MigrationUtil.getIds(feature, dbVendor, "get_archived_gateway_instances_in_unknown_state_id", parameters, sql)
             
                if (archGatesInUnknownState.isEmpty()) {  
                   println "Updating Gateway State in Database: " + failedGatewayId
                    MigrationUtil.executeSqlFile(feature, dbVendor, "update_gateway_instance_state", parameters, sql, false)
                    
                   println "Deleting related archived gateway states: " + failedGatewayId
                    MigrationUtil.executeSqlFile(feature, dbVendor, "delete_archived_gateway_instances", parameters, sql, false)
                } else {
                   println "**** Unknown gateway entry found for " + failedGatewayId + ", no database changes made"
                }
            }
            println "Done"
        }
    }
}


