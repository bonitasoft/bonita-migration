import org.bonitasoft.migration.core.MigrationUtil;

if ("mysql".equalsIgnoreCase(dbVendor)) {
    println("db vendor [$dbVendor]: remove constraint fk_message_instance_tenantId")
    MigrationUtil.executeDefaultSqlFile(feature, dbVendor, sql)
} else {
    println("db vendor [$dbVendor]: nothing to do.")
}

