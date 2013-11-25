import org.bonitasoft.migration.core.MigrationUtil;

def parameters = Collections.singletonMap(":creationDate", new Date().getTime());
MigrationUtil.executeSqlFile(feature, dbVendor, null, parameters, sql, true);

def currentTime = System.currentTimeMillis()
def tenants = MigrationUtil.getTenantsId(feature, dbVendor, sql)

println "Executing update for each tenant : " + tenants
tenants.each {
    println "For tenant with id = " + it
    //there is profile and profile entries needed
    def adminId = MigrationUtil.getId(feature, dbVendor, "get_admin_profile_id", it, sql)
    def directoryId = MigrationUtil.getId(feature, dbVendor, "get_dir_profile_entry_id", it, sql)

    if(adminId != null && directoryId != null){
        parameters = new HashMap()
        parameters.put(":tenantId", String.valueOf(it))
        parameters.put(":admin_profile_id", String.valueOf(adminId))
        parameters.put(":dir_profile_entry_id", String.valueOf(directoryId))
        MigrationUtil.executeSqlFile(feature, dbVendor, "update", parameters, sql, false)
    }
    println "Done"
}