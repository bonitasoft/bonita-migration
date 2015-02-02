import org.bonitasoft.migration.core.MigrationUtil;

MigrationUtil.executeDefaultSqlFile(feature, dbVendor, sql);

def tenants = MigrationUtil.getTenantsId(dbVendor, sql)

println "Executing update for each tenant : " + tenants
tenants.each {
    println "For tenant with id = " + it
    if(adminId != null && directoryId != null){
        parameters = [:]
        parameters.put(":tenantId", String.valueOf(it))
        MigrationUtil.executeSqlFile(feature, dbVendor, "update", parameters, sql, false)
    }
    println "Done"
}
