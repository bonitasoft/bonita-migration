import java.io.ObjectInputStream;
import java.util.Map;

import org.quartz.JobDataMap;

import groovy.sql.Sql;

import org.bonitasoft.migration.core.MigrationUtil;
import org.bonitasoft.migration.core.IOUtil;

println "Droping tables p_metadata_val and p_metadata_def"

def parameters = new HashMap()
MigrationUtil.executeSqlFile(feature, dbVendor, "cleanOldTables", parameters, sql, false)

println "Creating tables custom_usr_inf_def and custom_usr_inf_val"
MigrationUtil.executeSqlFile(feature, dbVendor, "createCustomUserInfoTables", parameters, sql, false)
