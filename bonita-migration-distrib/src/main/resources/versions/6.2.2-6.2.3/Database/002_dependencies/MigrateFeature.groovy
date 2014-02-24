import org.bonitasoft.migration.core.MigrationUtil;

def currentTime = System.currentTimeMillis()

println  '''Migrate all dependency for process, with bad naming (without <processId>_ as prefix)'''
def goodNameRegex = /[0-9]+_.+/
sql.eachRow("SELECT * FROM dependency ") { dep ->
    if(!(dep.name ==~ goodNameRegex)){
        sql.eachRow("SELECT * FROM dependencymapping WHERE tenantid = ${dep.tenantid} AND dependencyid = ${dep.id} ") { mapping ->
            if(mapping.artifacttype == "process"){
                def newName = mapping.artifactid+"_"+dep.name
                def newFileName = mapping.artifactid+"_"+dep.filename
                sql.executeUpdate("UPDATE dependency SET name = $newName, filename = $newFileName WHERE tenantid = ${dep.tenantid} AND id = ${dep.id}")
            }
        }
    }
}

println  '''Migrate all dependency mapping with "process" (replace by "PROCESS"), "tenant" (replace by "TENANT"), "_global_" (platform dependency) (replace by "GLOBAL")'''
println sql.executeUpdate("UPDATE dependencymapping SET artifacttype = 'PROCESS' WHERE artifacttype = 'process'") + " process dependency mapping updated"
println sql.executeUpdate("UPDATE dependencymapping SET artifacttype = 'TENANT' WHERE artifacttype = 'tenant'") + " tenant dependency mapping updated"
println sql.executeUpdate("UPDATE pdependencymapping SET artifacttype = 'GLOBAL' WHERE artifacttype = '_global_'") + " platform dependency mapping updated"

println "Drop column version"
if(dbVendor.equals("sqlserver")){
    sql.executeUpdate("DROP INDEX idx_dependency_version ON dependency")
    sql.executeUpdate("DROP INDEX idx_pdependency_version ON pdependency")
}
sql.executeUpdate("ALTER TABLE dependency DROP COLUMN version")
sql.executeUpdate("ALTER TABLE pdependency DROP COLUMN version")
