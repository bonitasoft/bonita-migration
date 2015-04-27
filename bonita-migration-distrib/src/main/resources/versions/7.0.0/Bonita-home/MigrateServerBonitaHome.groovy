import org.bonitasoft.migration.core.IOUtil

import java.nio.file.Path

def File newBonitaHome = new File(newBonitaHome, "/engine-server/")
def File newBonitaHomeDestination = new File(bonitaHome, "/engine-server/")
def File bonitaHomeToMigrate = new File(bonitaHome, "/server/")

//Copy original engine-server
IOUtil.copyDirectory(newBonitaHome, newBonitaHomeDestination);


move(new File(bonitaHomeToMigrate.path + "/platform/work"), new File(newBonitaHomeDestination.path + "/work/platform"))
def tenantsFolder = new File(bonitaHomeToMigrate, "tenants")
tenantsFolder.listFiles().each { tenantFolder ->
    def tenantId = Long.valueOf(tenantFolder.getName())
    println "migrating tenant: " + tenantId
    //MOVE:  server/tenants/X/work/processes -> engine-server/work/tenants/X/processes
    move(new File(tenantFolder.path + "/work/processes"), new File(newBonitaHomeDestination.path + "/work/tenants/$tenantId/processes"))
    //MOVE:  server/tenants/X/work/security-scripts-> engine-server/work/tenants/X/security-scripts
    move(new File(tenantFolder.path + "/work/security-scripts"), new File(newBonitaHomeDestination.path + "/work/tenants/$tenantId/security-scripts"))
    //Create:  X -> engine-server/work/tenants/X/bonita-tenant-id.properties that contains tenantId=X

    def tenantProperties = newBonitaHomeDestination.path + "/work/tenants/$tenantId/bonita-tenant-id.properties"
    println "Write file $tenantProperties"
    new File(tenantProperties).write("tenantId=$tenantId")
    //Copy:  X -> engine-server/work/tenants/template/* -> engine-server/work/tenants/X/.
    IOUtil.copyDirectory(new File(newBonitaHomeDestination.path + "/work/tenants/template"), new File(newBonitaHomeDestination.path + "/work/tenants/$tenantId"));
    //Copy:  X -> engine-server/conf/tenants/template/* -> engine-server/conf/tenants/X/.
    IOUtil.copyDirectory(new File(newBonitaHomeDestination.path + "/conf/tenants/template"), new File(newBonitaHomeDestination.path + "/conf/tenants/$tenantId"));
}
IOUtil.deleteDirectory(tenantsFolder)
IOUtil.deleteDirectory(new File(bonitaHomeToMigrate.path + "/platform/"))

def move(File src, File dest) {
    if (src.exists()) {
        println "Move $src to $dest"
        IOUtil.copyDirectory(src, dest)
        IOUtil.deleteDirectory(src)
    }else {
        println "src file $src does not exists"
    }
}

