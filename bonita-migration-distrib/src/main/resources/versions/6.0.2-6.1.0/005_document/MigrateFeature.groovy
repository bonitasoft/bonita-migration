import org.bonitasoft.migration.core.MigrationUtil;

MigrationUtil.executeDefaultSqlFile(feature, dbVendor, sql)
//get the path from bonita home (default = bonita.home/platform/work)
//for each row in document_mapping get the corresponding file content
def contentRoot = new File(new File(new File(bonitaHome, "server"), "platform"), "work")
def contents = []
def Map idByTenant = [:]
def migrateContentFor = { table, contentIndex ->
    sql.eachRow("SELECT * FROM " + table) { row ->
        if(row[6]){//document has content
            def contentId = row[contentIndex]
            def tenantId = row[0]
            idByTenant.put(tenantId, idByTenant.get(tenantId) == null ? 1 : idByTenant.get(tenantId) + 1)
            def File content = new File(contentRoot, contentId)
            if(!content.exists()){
                throw new IllegalStateException("content not found " + content.getAbsolutePath())
            }
            def sqlFile = MigrationUtil.getSqlFile(feature, dbVendor, "insertcontent")
            sql.executeInsert(MigrationUtil.getSqlContent(sqlFile.text, null)[0], tenantId, idByTenant.get(tenantId), contentId, content.getBytes())
            contents.add(content)
        }
    }
}
//execute for live document mappings
migrateContentFor("document_mapping", 9)
//execute for archived document mappings
migrateContentFor("arch_document_mapping", 10)
println "updating sequence"
//ids are same for archived mapping of live mapping, all is in document content
idByTenant.each {
    def tenantId= it.key
    def nbElements = it.value
    println "update sequence for tenantId " + tenantId + " with nextId=" + (nbElements + 1)
    def parameters = new HashMap()
    parameters.put(":tenantId", String.valueOf(tenantId));
    parameters.put(":nextId", String.valueOf(nbElements + 1));
    MigrationUtil.executeSqlFile(feature, dbVendor, "updateSequence", parameters, sql, true)
}
//deleting files
contents.each { it.delete() }
println "migrated " + contents.size() + " documents from file system to database"