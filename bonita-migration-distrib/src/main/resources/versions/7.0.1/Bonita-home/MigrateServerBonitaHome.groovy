import org.bonitasoft.migration.core.IOUtil

def File newBonitaHome = new File(newBonitaHome, "/engine-server/")
def File bonitaHomeToMigrate = new File(bonitaHome, "/engine-server/")

String WORK_PLATFORM =  "/work/platform"

//Copy original engine-server
IOUtil.copyDirectory(newBonitaHome, bonitaHomeToMigrate);


def tenantsFolder = new File(bonitaHomeToMigrate, "work/tenants")
tenantsFolder.listFiles().each { tenantFolder ->
    if(!tenantFolder.getName().equals("template")) {
        def tenantId = Long.valueOf(tenantFolder.getName())
        println "migrating tenant: " + tenantId
        //MOVE:  server/tenants/X/work/processes -> engine-server/work/tenants/X/processes
        move(new File(tenantFolder.path + "/work/processes"), new File(bonitaHomeToMigrate.path + "/work/tenants/$tenantId/processes"))
        //MOVE:  server/tenants/X/work/security-scripts-> engine-server/work/tenants/X/security-scripts
        move(new File(tenantFolder.path + "/work/security-scripts"), new File(bonitaHomeToMigrate.path + "/work/tenants/$tenantId/security-scripts"))
        //Create:  X -> engine-server/work/tenants/X/bonita-tenant-id.properties that contains tenantId=X

        def tenantProperties = bonitaHomeToMigrate.path + "/work/tenants/$tenantId/bonita-tenant-id.properties"
        println "Write file $tenantProperties"
        new File(tenantProperties).write("tenantId=$tenantId")
        //Copy:  X -> engine-server/work/tenants/template/* -> engine-server/work/tenants/X/.
        println "Moving ${new File(bonitaHomeToMigrate.path + "/work/tenants/template").path} to ${new File(bonitaHomeToMigrate.path + "/work/tenants/$tenantId").path}"
        IOUtil.copyDirectory(new File(bonitaHomeToMigrate.path + "/work/tenants/template"), new File(bonitaHomeToMigrate.path + "/work/tenants/$tenantId"));
        //Copy:  X -> engine-server/conf/tenants/template/* -> engine-server/conf/tenants/X/.
        IOUtil.copyDirectory(new File(bonitaHomeToMigrate.path + "/conf/tenants/template"), new File(bonitaHomeToMigrate.path + "/conf/tenants/$tenantId"));
    }
}

def move(File src, File dest) {
    if (src.exists()) {
        println "Move $src to $dest"
        IOUtil.copyDirectory(src, dest)
        IOUtil.deleteDirectory(src)
    }else {
        println "src file $src does not exists"
    }
}

