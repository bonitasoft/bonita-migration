package org.bonitasoft.migration.core
/**
 * @author Baptiste Mesta
 */
abstract class VersionMigration {

    Logger logger
    String version
    MigrationContext context

    abstract List<MigrationStep> getMigrationSteps()


    def migrateBonitaHome() {
        def dir = File.createTempDir()

        def stream1 = this.class.getResourceAsStream("/homes/bonita-home-${version}-full.zip")
        if(stream1 == null){
            logger.warn("Using snapshot version of the migration tool.")
            stream1 = this.class.getResourceAsStream("/homes/bonita-home-${version}-SNAPSHOT-full.zip")
        }
        if(stream1 == null){
            throw new IllegalStateException("There is no bonita home available for the version $version")
        }
        IOUtil.unzip(stream1, dir)

        File newBonitaHome = new File(dir, "bonita-home")
        migrateBonitaHomeClient(newBonitaHome)
        migrateBonitaHomeServer(newBonitaHome);

        dir.delete()
    }

    def migrateBonitaHomeClient(File newBonitaHome) {
        def bonitaHome = context.bonitaHome
        def currentDir = ""
        def File newWebClientBonitaHome = new File(newBonitaHome, "/client/")
        def File newEngineClientBonitaHome = new File(newBonitaHome, "/engine-client/")
        def File oldClientBonitaHome = new File(bonitaHome, "/client/")

// REPLACE: client/conf -> engine-client/conf/
        IOUtil.copyDirectory(newEngineClientBonitaHome, new File(bonitaHome, "/engine-client/"))
        IOUtil.deleteDirectory(new File(oldClientBonitaHome.path + "/conf"))

//deactivate the security if it was not active
        println "Check if security was enabled"
        def oldSecurityFile = new File(oldClientBonitaHome.path + "/platform/tenant-template/conf/security-config.properties")
        def newSecurityFile = new File(newWebClientBonitaHome.path + "/platform/tenant-template/conf/security-config.properties")
        def props = new Properties()
        if (oldSecurityFile.exists()) {
            oldSecurityFile.withDataInputStream { s ->
                props.load(s)
            }
        }
        def secuEnabled = props.getProperty("security.rest.api.authorizations.check.enabled")
        if (!"true".equals(secuEnabled)) {
            if (secuEnabled == null) {
                println "Security of REST APIs is disabled, to enable it modify the file security-config.properties in the client tenant configuration folder of the bonita home"
            }
            def newProps = new Properties()
            newSecurityFile.withDataInputStream { s ->
                newProps.load(s)
                newProps.setProperty("security.rest.api.authorizations.check.enabled", "false")
                newProps.store(newSecurityFile.newWriter(), null);
            }
        }

        currentDir = "/platform/conf"
        MigrationUtil.migrateDirectory(newWebClientBonitaHome.path + currentDir, oldClientBonitaHome.path + currentDir, true)

        currentDir = "/platform/tenant-template"
        MigrationUtil.migrateDirectory(newWebClientBonitaHome.path + currentDir, oldClientBonitaHome.path + currentDir, true)

        currentDir = "/platform/work"
        MigrationUtil.migrateDirectory(newWebClientBonitaHome.path + currentDir, oldClientBonitaHome.path + currentDir, false)

        println "Checking for tenants."
        def tenantsClientDir = new File(oldClientBonitaHome, "/tenants")
        if (tenantsClientDir.exists()) {
            def tenants = Arrays.asList(tenantsClientDir.listFiles())
            if (tenants.empty) {
                println "No tenants found."
            } else {
                println "Executing update for each tenant : " + tenants
                tenantsClientDir.eachDir { tenant ->
                    println "For tenant : " + tenant.name
                    PrintStream stdout = IOUtil.executeWrappedWithTabs {
                        currentDir = "/conf"
                        MigrationUtil.migrateDirectory(newWebClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir, true)

                        currentDir = "/work/icons/default"
                        MigrationUtil.migrateDirectory(newWebClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir, true)

                        currentDir = "/work/icons/priority"
                        MigrationUtil.migrateDirectory(newWebClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir, true)

                        currentDir = "/work/icons/profiles"
                        MigrationUtil.migrateDirectory(newWebClientBonitaHome.path + "/platform/tenant-template" + currentDir, tenant.path + currentDir, true)
                    }
                }
            }
        } else {
            println "Not found any tenants."
        }
    }

    def migrateBonitaHomeServer(File newBonitaHome) {
        def bonitaHomeToMigrate = context.bonitaHome
//Copy original engine-server
        IOUtil.copyDirectory(newBonitaHome, bonitaHomeToMigrate);


        def tenantsFolder = new File(bonitaHomeToMigrate, "work/tenants")
        tenantsFolder.listFiles().each { tenantFolder ->
            if (!tenantFolder.getName().equals("template")) {
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
    }

    def move(File src, File dest) {
        if (src.exists()) {
            println "Move $src to $dest"
            IOUtil.copyDirectory(src, dest)
            IOUtil.deleteDirectory(src)
        } else {
            println "src file $src does not exists"
        }
    }
}
