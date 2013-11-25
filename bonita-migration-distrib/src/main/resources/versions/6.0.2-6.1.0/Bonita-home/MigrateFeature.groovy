import org.bonitasoft.migration.core.MigrationUtil;


println "Some folders will be deleted, you should make a backup of your bonita home."
println "If you have customized the configuration, reapply it after the migration is done."
if(!MigrationUtil.isAutoAccept()){
    println "Press ENTER to continue..."
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

