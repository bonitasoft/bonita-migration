import org.bonitasoft.migration.core.MigrationUtil;
import org.bonitasoft.migration.core.IOUtil;


def tenantsId = MigrationUtil.getTenantsId(dbVendor, sql)

println "Executing update for each tenant : " + tenantsId
IOUtil.executeWrappedWithTabs {
    tenantsId.each {
        println "For tenant with id = " + it
        
        // Modifier l'appel au script get_failed_gateway_instances_id pour récupérer id and hitBys
        
        // Get failed gateway with id and hitBys 
        def parameters = Collections.singletonMap(":tenantId", String.valueOf(it))
        def List<Long> failedGatewaysId = MigrationUtil.getIds(feature, dbVendor, "get_failed_gateway_instances", parameters, sql)
        println "Found " + failedGatewaysId.size() + " failed gateways."
            
        for (Long failedGatewayId : failedGatewaysId) {
            // Get the gateways in unknown state
            parameters = new HashMap()
            parameters.put(":tenantId", String.valueOf(it))
            parameters.put(":flowNodeInstanceId", String.valueOf(failedGatewayId))
            def List<Long> archGatesInUnknownState = MigrationUtil.getIds(feature, dbVendor, "get_archived_gateway_instances_in_unknown_state_id", parameters, sql)
             
            if (archGatesInUnknownState.isEmpty()) {  
                println "Updating the state of the gateway with the id = " + failedGatewayId
                def parametersWithState = parameters
                
                // Si le hitBys commence par FINISH:, executer le script avec :state à 'completed'
                parameters.put(":state", "'completed'")
                // Sinon, avec :state à 'executing'
                parameters.put(":state", "'executing'")
                
                MigrationUtil.executeSqlFile(feature, dbVendor, "update_gateway_instance_state", parametersWithState, sql, false)
                    
                
                
                println "Deleting the archived gateway related to the gateway with the id = " + failedGatewayId
                def parametersWithStates = parameters
                // Si le hitBys commence par FINISH:, ne pas supprimer l'état executing des archives
                parameters.put(":states", "'completed', 'failed'")
                // Sinon, Supprimer tous les états avec le script : delete_archived_gateway_instances
                parameters.put(":states", "'completed', 'failed', 'executing'")
                
                MigrationUtil.executeSqlFile(feature, dbVendor, "delete_archived_gateway_instances", parametersWithStates, sql, false)
            } else {
                println "**** Unknown gateway entry found for " + failedGatewayId + ", no database changes made"
            }
        }
        println "Done"
    }
}
