import org.bonitasoft.migration.core.MigrationUtil;

if(!MigrationUtil.isAutoAccept()){
    println "Press ENTER to continue or Ctrl+C to cancel."
    System.console().readLine()
}
println ""

def binding = new Binding(["bonitaHome":bonitaHome, "feature":feature, "startMigrationDate":startMigrationDate]);

// Migration of client bonita home
println "[ Migrating <Client bonita home> 1/2 ]"
MigrationUtil.executeMigration(gse, feature, "MigrateClientBonitaHome.groovy", binding, 3, startMigrationDate);

// Migration of server bonita home
println "[ Migrating <Server bonita home> 2/2 ]"
MigrationUtil.executeMigration(gse, feature, "MigrateServerBonitaHome.groovy", binding, 3, startMigrationDate);

