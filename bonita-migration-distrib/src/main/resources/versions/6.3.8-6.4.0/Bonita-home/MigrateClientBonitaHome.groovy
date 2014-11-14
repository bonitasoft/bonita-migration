import org.bonitasoft.migration.core.MigrationUtil;
import org.bonitasoft.migration.core.IOUtil;

def currentDir = ""

def File newClientBonitaHome = new File(newBonitaHome, "/client/")
def File oldClientBonitaHome = new File(bonitaHome, "/client/")

//deactivate the security if it was not active
println "Check if security was enabled"
def oldSecurityFile = new File(oldClientBonitaHome.path + "/platform/tenant-template/conf/security-config.properties")
def newSecurityFile = new File(newClientBonitaHome.path + "/platform/tenant-template/conf/security-config.properties")
def props = new Properties()
if(oldSecurityFile.exists()) {
    oldSecurityFile.withDataInputStream { s ->
        props.load(s)
    }
}
def secuEnabled = props.getProperty("security.rest.api.authorizations.check.enabled")
if(!"true".equals(secuEnabled)){
    if(secuEnabled == null){
        println "Security of REST APIs is disabled, to enable it modify the file security-config.properties in the client tenant configuration folder of the bonita home"
    }
    def newProps = new Properties()
    newSecurityFile.withDataInputStream { s->
        newProps.load(s)
        newProps.setProperty("security.rest.api.authorizations.check.enabled", "false")
        newProps.store(newSecurityFile.newWriter(), null);
    }
}

currentDir = "/platform/conf"
MigrationUtil.migrateDirectory(newClientBonitaHome.path + currentDir, oldClientBonitaHome.path + currentDir, true)

currentDir = "/platform/tenant-template"
MigrationUtil.migrateDirectory(newClientBonitaHome.path + currentDir, oldClientBonitaHome.path + currentDir, true)

currentDir = "/platform/work"
MigrationUtil.migrateDirectory(newClientBonitaHome.path + currentDir, oldClientBonitaHome.path + currentDir, false)

println "Checking for tenants."
def tenantsClientDir = new File(oldClientBonitaHome, "/tenants")
if (tenantsClientDir.exists()) {
    def tenants = Arrays.asList(tenantsClientDir.listFiles())
    if (tenants.empty){
        println "No tenants found."
    } else {
        println "Executing update for each tenant : " + tenants
        tenantsClientDir.eachDir { tenant ->
            println "For tenant : " + tenant.name
            PrintStream stdout = IOUtil.executeWrappedWithTabs {
                currentDir = "/conf"
                MigrationUtil.migrateDirectory(newClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir, true)

                currentDir = "/work/icons/default"
                MigrationUtil.migrateDirectory(newClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir, true)

                currentDir = "/work/icons/priority"
                MigrationUtil.migrateDirectory(newClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir, true)

                currentDir = "/work/icons/profiles"
                MigrationUtil.migrateDirectory(newClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir, true)
            }
        }
    }
} else {
    println "Not found any tenants."
}