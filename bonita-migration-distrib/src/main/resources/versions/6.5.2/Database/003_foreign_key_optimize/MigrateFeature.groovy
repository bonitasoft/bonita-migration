import org.bonitasoft.migration.core.MigrationUtil;

if (dbVendor.equals("mysql")) {
    MigrationUtil.executeDefaultSqlFile(feature, dbVendor, sql)
}
//nothing to do on other dbms
