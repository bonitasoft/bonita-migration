import org.bonitasoft.migration.core.MigrationUtil;

if ("oracle".equals(dbVendor)){
    sql.eachRow("select u.CONSTRAINT_NAME from user_constraints u where upper(TABLE_NAME)='PAGE' and constraint_type='U'") { row ->
        def constraintName = row[0]
        println "removing constraint PAGE.${constraintName} (oracle only)"
        sql.execute("ALTER TABLE page DROP CONSTRAINT "+ constraintName )
    }
}
MigrationUtil.executeDefaultSqlFile(feature, dbVendor, sql);

