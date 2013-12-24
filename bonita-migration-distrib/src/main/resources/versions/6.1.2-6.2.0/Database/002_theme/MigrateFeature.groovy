import org.bonitasoft.migration.core.MigrationUtil;

MigrationUtil.executeDefaultSqlFile(feature, dbVendor, sql)


def currentTime = System.currentTimeMillis()
def tenants = MigrationUtil.getTenantsId(feature, dbVendor, sql)

println "Executing update for each tenant : " + tenants
tenants.each {
    println "For tenant with id = " + it

    // Portal
    def zip = new File(feature, "bonita-portal-theme.zip")
    def css = new File(feature, "bonita.css")

    parameters = new HashMap()
    parameters.put(":tenantId", String.valueOf(it))
    parameters.put(":content", zip.bytes)
    parameters.put(":cssContent", css.bytes)
    parameters.put(":type", "PORTAL")
    parameters.put(":lastUpdateDate", currentTime)
    MigrationUtil.executeSqlFile(feature, dbVendor, "update", parameters, sql, false)

    println "Done"
}