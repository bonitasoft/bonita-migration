import org.bonitasoft.migration.core.MigrationUtil;

def currentDir = ""

def File newClientBonitaHome = new File(newBonitaHome, "/client/");
def File oldClientBonitaHome = new File(bonitaHome, "/client/");

currentDir = "/platform/conf"
MigrationUtil.migrateDirectory(newClientBonitaHome.path + currentDir, oldClientBonitaHome.path + currentDir, true)

currentDir = "/platform/tenant-template"
MigrationUtil.migrateDirectory(newClientBonitaHome.path + currentDir, oldClientBonitaHome.path + currentDir, true)

currentDir = "/platform/work"
MigrationUtil.migrateDirectory(newClientBonitaHome.path + currentDir, oldClientBonitaHome.path + currentDir, false)

println "Checking for tenants."
def tenantsClientDir = new File(oldClientBonitaHome, "/tenants")
if (tenantsClientDir.exists()) {
    def tenants = Arrays.asList(tenantsClientDir.listFiles());
    if (tenants.empty){
        println "No tenants found."
    } else {
        println "Executing update for each tenant : " + tenants;
        tenantsClientDir.eachFile { tenant ->
            println "For tenant : " + tenant.name
            PrintStream stdout = MigrationUtil.executeWrappedWithTabs {
                currentDir = "/conf"
                MigrationUtil.migrateDirectory(newClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir, true)

                currentDir = "/work/icons/default"
                MigrationUtil.migrateDirectory(newClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir, true)

                currentDir = "/work/icons/priority"
                MigrationUtil.migrateDirectory(newClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir, true)

                currentDir = "/work/icons/profiles"
                MigrationUtil.migrateDirectory(newClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir, true)
                
                currentDir = "/work/looknfeel"
                MigrationUtil.migrateDirectory(newClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir, true)
                
                currentDir = "/work/reports"
                fromDir = newClientBonitaHome.path + "/platform/tenant-template" + currentDir
                if(new File(fromDir).exists()){
                    MigrationUtil.migrateDirectory(fromDir, tenant.path + currentDir, true)
                }
            }
        }
    }
} else {
    println "Not found any tenants."
}