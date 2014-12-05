import org.bonitasoft.migration.core.MigrationUtil;

def parameters = [:]
MigrationUtil.executeSqlFile(feature, dbVendor, "update", parameters, sql, false)
