import org.bonitasoft.migration.core.MigrationUtil;

def binding = new Binding(["bonitaHome":bonitaHome, "feature":feature, "startMigrationDate":startMigrationDate]);

// Migration of client bonita home
println "[ Migrating <Client bonita home> 1/2 ]"
MigrationUtil.executeMigration(gse, feature, "MigrateClientBonitaHome.groovy", binding, startMigrationDate);

// Migration of server bonita home
println "[ Migrating <Server bonita home> 2/2 ]"
MigrationUtil.executeMigration(gse, feature, "MigrateServerBonitaHome.groovy", binding, startMigrationDate);

