import org.bonitasoft.migration.core.IOUtil
import org.bonitasoft.migration.core.MigrationUtil

def currentDir = ""

def File newBonitaHome = new File(newBonitaHome, "/engine-server/")
def File newBonitaHomeDestination = new File(bonitaHome, "/engine-server/")
def File bonitaHomeToMigrate = new File(bonitaHome, "/server/")

IOUtil.copyDirectory(bonitaHomeToMigrate, newBonitaHomeDestination)
IOUtil.deleteDirectory(bonitaHomeToMigrate)
bonitaHomeToMigrate = newBonitaHomeDestination
MigrationUtil.migrateDirectory(newBonitaHome.path + currentDir, bonitaHomeToMigrate.path + currentDir, true)



currentDir = "/platform/conf"
MigrationUtil.migrateDirectory(newBonitaHome.path + currentDir, bonitaHomeToMigrate.path + currentDir, true)

currentDir = "/platform/tenant-template"
MigrationUtil.migrateDirectory(newBonitaHome.path + currentDir, bonitaHomeToMigrate.path + currentDir, true)

println "Detecting tenants..."
def tenantsServerDir = new File(bonitaHomeToMigrate, "/tenants")
if (tenantsServerDir.exists()) {
    def tenants = Arrays.asList(tenantsServerDir.listFiles());
    if (tenants.empty) {
        println "No tenant found."
    } else {
        println "Executing update for each tenant : " + tenants;
        tenantsServerDir.eachFile { tenant ->
            println "For tenant : " + tenant.name
            IOUtil.executeWrappedWithTabs {
                MigrationUtil.migrateDirectory(newBonitaHome.path + "/platform/tenant-template/conf", tenant.path + "/conf", true)
            }
        }
    }
} else {
    println "No tenant found."
}