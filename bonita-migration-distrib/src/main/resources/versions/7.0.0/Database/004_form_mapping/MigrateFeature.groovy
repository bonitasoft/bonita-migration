import org.bonitasoft.migration.core.MigrationUtil
import org.bonitasoft.migration.versions.v7_0_0.FormMappingMigration;

MigrationUtil.executeDefaultSqlFile(feature, dbVendor, sql);

def tenants = MigrationUtil.getTenantsId(dbVendor, sql)
println "Executing update for each tenant : " + tenants
tenants.each {
    println "For tenant with id = " + it
    def parameters = new HashMap()
    parameters.put(":tenantId", String.valueOf(it))
    MigrationUtil.executeSqlFile(feature, dbVendor, "update", parameters, sql, false)
    println "Done"
}
new FormMappingMigration(sql, dbVendor, bonitaHome).migrate()
