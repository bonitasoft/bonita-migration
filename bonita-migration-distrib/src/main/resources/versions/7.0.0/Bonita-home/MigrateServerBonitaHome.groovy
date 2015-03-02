import org.bonitasoft.migration.core.MigrationUtil;
import org.bonitasoft.migration.core.IOUtil;

def currentDir = ""

def File newServerBonitaHome = new File(newBonitaHome, "/server/");
def File oldServerBonitaHome = new File(bonitaHome, "/server/");


currentDir = "/platform/conf"
MigrationUtil.migrateDirectory(newServerBonitaHome.path + currentDir, oldServerBonitaHome.path + currentDir, true)

currentDir = "/platform/tenant-template"
MigrationUtil.migrateDirectory(newServerBonitaHome.path + currentDir, oldServerBonitaHome.path + currentDir, true)

println "Migrating business data compilation dependencies to new version"
currentDir = "/platform/data-management"
MigrationUtil.migrateDirectory(newServerBonitaHome.path + currentDir, oldServerBonitaHome.path + currentDir, true)

println "Detecting tenants..."
def tenantsServerDir = new File(oldServerBonitaHome, "/tenants")
if (tenantsServerDir.exists()) {
    def tenants = Arrays.asList(tenantsServerDir.listFiles());
    if (tenants.empty){
        println "Not found any tenants."
    } else {
        println "Executing update for each tenant : " + tenants;
        tenantsServerDir.eachFile { tenant ->
            println "For tenant : " + tenant.name
            PrintStream stdout = IOUtil.executeWrappedWithTabs {
                MigrationUtil.migrateDirectory(newServerBonitaHome.path + "/platform/tenant-template/conf", tenant.path + "/conf", true)
            }
        }
    }
} else {
    println "Not found any tenants."
}