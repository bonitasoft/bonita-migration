import org.bonitasoft.migration.core.MigrationUtil;

if(!MigrationUtil.isAutoAccept()){
    println "Press ENTER to continue or Ctrl+C to cancel."
    System.console().readLine()
}
println ""

//the new bonita home
def newBonitaHome = feature.listFiles().findAll { it.isDirectory() && it.exists() && it.getName().startsWith("bonita")}[0]
def binding = new Binding(["bonitaHome":bonitaHome, "feature":feature, "newBonitaHome":newBonitaHome, "startMigrationDate":startMigrationDate]);
//extra setup of bonita home
def setupScript = new File(feature, "setup.groovy")
if(setupScript.exists()){
    println "executing setup of bonita home"
    gse.run(setupScript.getPath(), binding)
}

// Migration of client bonita home
println "[ Migrating <Client bonita home> 1/2 ]"
MigrationUtil.executeMigration(gse, feature, "MigrateClientBonitaHome.groovy", binding, 3, startMigrationDate);

// Migration of server bonita home
println "[ Migrating <Server bonita home> 2/2 ]"
MigrationUtil.executeMigration(gse, feature, "MigrateServerBonitaHome.groovy", binding, 3, startMigrationDate);

