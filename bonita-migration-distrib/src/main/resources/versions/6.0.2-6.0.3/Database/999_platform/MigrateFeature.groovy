import org.bonitasoft.migration.core.MigrationUtil;

def parameters = Collections.singletonMap(":version", "6.0.3")
MigrationUtil.executeSqlFile(feature, dbVendor, null, parameters, sql, true)