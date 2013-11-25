import org.bonitasoft.migration.core.MigrationUtil;

def currentDir = ""

def File newClientBonitaHome = new File(feature, "bonita/client/");
def File oldClientBonitaHome = new File(bonitaHome, "/client/");

currentDir = "/platform/conf"
MigrationUtil.migrateDirectory(newClientBonitaHome.path + currentDir, oldClientBonitaHome.path + currentDir)

currentDir = "/platform/tenant-template"
MigrationUtil.migrateDirectory(newClientBonitaHome.path + currentDir, oldClientBonitaHome.path + currentDir)

currentDir = "/platform/work"
MigrationUtil.migrateDirectory(newClientBonitaHome.path + currentDir, oldClientBonitaHome.path + currentDir)

println "Detecting tenants..."
def tenantsClientDir = new File(oldClientBonitaHome, "/tenants")
if (tenantsClientDir.exists()) {
    def tenants = Arrays.asList(tenantsClientDir.listFiles());
    if (tenants.empty){
        println "Not found any tenants."
    } else {
        println "Executing update for each tenant : " + tenants;
        tenantsClientDir.eachFile { tenant ->
            println "For tenant : " + tenant.name
            PrintStream stdout = MigrationUtil.setSystemOutWithTab(4);

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
            System.setOut(stdout);
        }
    }
} else {
    println "Not found any tenants."
}