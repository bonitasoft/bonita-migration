
def currentDir = ""

return;

// Migration of client bonita home
println "Starting the client bonita home migration"

def File newClientBonitaHome = new File(feature, "bonita/client/");
def File oldClientBonitaHome = new File(bonitaHome, "/client/");

currentDir = "/platform/conf"
MigrationUtil.migrateDirectory(newClientBonitaHome.path + currentDir, oldClientBonitaHome.path + currentDir)

currentDir = "/platform/tenant-template"
MigrationUtil.migrateDirectory(newClientBonitaHome.path + currentDir, oldClientBonitaHome.path + currentDir)

currentDir = "/platform/work"
MigrationUtil.migrateDirectory(newClientBonitaHome.path + currentDir, oldClientBonitaHome.path + currentDir)

println "Detecting tenants"

def tenantsClientDir = new File(oldClientBonitaHome, "/tenants")
if (tenantsClientDir.exists()) {
    tenantsClientDir.eachFile { tenant ->
        println "Found tenant: " + tenant.name

        currentDir = "/conf"
        MigrationUtil.migrateDirectory(newClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir)

        currentDir = "/work/icons/default"
        MigrationUtil.migrateDirectory(newClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir)

        currentDir = "/work/icons/priority"
        MigrationUtil.migrateDirectory(newClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir)

        currentDir = "/work/icons/profiles"
        MigrationUtil.migrateDirectory(newClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir)

        currentDir = "/work/looknfeel"
        MigrationUtil.migrateDirectory(newClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir)
    }
}

println "Client bonita home succesfully migrated"


// Migration of server bonita home
println "Starting the server bonita home migration"

def File newServerBonitaHome = new File(feature, "bonita/server/");
def File oldServerBonitaHome = new File(bonitaHome, "/server/");

currentDir = "/platform/conf"
MigrationUtil.migrateDirectory(newServerBonitaHome.path + currentDir, oldServerBonitaHome.path + currentDir)

currentDir = "/platform/tenant-template"
MigrationUtil.migrateDirectory(newServerBonitaHome.path + currentDir, oldServerBonitaHome.path + currentDir)

println "Detecting tenants"

def tenantsServerDir = new File(oldServerBonitaHome, "/tenants")
if (tenantsServerDir.exists()) {
    tenantsServerDir.eachFile { tenant ->
        MigrationUtil.migrateDirectory(newServerBonitaHome.path + "/platform/tenant-template/conf", tenant.path + "/conf")
    }
}
println "Server bonita home succesfully migrated"

