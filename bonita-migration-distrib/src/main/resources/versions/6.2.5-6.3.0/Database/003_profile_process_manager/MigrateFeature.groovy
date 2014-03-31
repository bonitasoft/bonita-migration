import org.bonitasoft.migration.core.MigrationUtil;

def currentTime = System.currentTimeMillis()
def tenants = MigrationUtil.getTenantsId(feature, dbVendor, sql)

println "Executing update for each tenant : " + tenants
tenants.each {
    println "For tenant with id = " + it
    
    // Create the new profile in first
    def parameters = new HashMap();
    parameters.put(":tenantId", String.valueOf(it))
    parameters.put(":creationDate", new Date().getTime())
    MigrationUtil.executeSqlFile(feature, dbVendor, "insert_process-manager-profile", parameters, sql, false)
    
    
    // There is a needed profile 
    def processManagerId = MigrationUtil.getId(feature, dbVendor, "get_process_manager_profile_id", it, sql)

    
    // Then, create its profile entries
    if(processManagerId != null){
        parameters = new HashMap()
        parameters.put(":tenantId", String.valueOf(it))
        parameters.put(":process_manager_profile_id", String.valueOf(processManagerId))
        MigrationUtil.executeSqlFile(feature, dbVendor, "insert_profileentry_for_process_manager", parameters, sql, false)
    }
    println "Done"
}