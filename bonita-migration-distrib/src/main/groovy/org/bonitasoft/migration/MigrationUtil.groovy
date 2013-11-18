package org.bonitasoft.migration

import groovy.sql.Sql

import java.io.File;
import java.sql.ResultSet


public class MigrationUtil {


	public static String DB_URL = "db.url";

	public static String DB_USER = "db.user";

	public static String DB_PASSWORD = "db.password";

	public static String DB_DRIVERCLASS = "db.driverclass";

	public static String DB_VENDOR = "db.vendor";

	public static String BONITA_HOME = "bonita.home";

	public static String REQUEST_SEPARATOR = "@@";


	public static Map parseOrAskArgs(String[] args){
		//will ask for missing parameter
		return listToMap(args);
	}

	public static Map listToMap(String[] list){
		def map = [:];
		def iterator = list.iterator()
		while (iterator.hasNext()) {
			map.put(iterator.next().substring(2),iterator.next());
		}
		return map;
	}

	public static Sql getSqlConnection(Map props){
		def dburl = props.get(MigrationUtil.DB_URL);
		def user = props.get(MigrationUtil.DB_USER);
		def pass = props.get(MigrationUtil.DB_PASSWORD);
		def driver = props.get(MigrationUtil.DB_DRIVERCLASS);
		println "url=" + dburl;
		println "user=" + user;
		println "pass=" + pass;
		println "driver=" + driver;
		return Sql.newInstance(dburl, user, pass, driver);
	}

	public static executeDefaultSqlFile(File file, String dbVendor, groovy.sql.Sql sql){
		def sqlFile = getSqlFile(file, dbVendor, null);
		executeContentFile(sqlFile, sql, null);
	}
	
	public static executeSqlFile(File file, String dbVendor, String suffix, Map<String, String> parameters, groovy.sql.Sql sql){
		def sqlFile = getSqlFile(file, dbVendor, suffix);
		executeContentFile(sqlFile, sql, parameters);
	}

	private static executeContentFile(File sqlFile, groovy.sql.Sql sql, Map<String, String> parameters){
		sql.withTransaction {
			if(sqlFile.exists()){
				def sqlFileContent = replaceParameters(sqlFile, parameters).replaceAll("\r\n", "\n");
				def contents = sqlFileContent.split(REQUEST_SEPARATOR);

				for (content in contents) {
					if (!content.trim().empty) {
						println sql.executeUpdate(content) + " row(s) updated";
					}
				}
			} else{
				println "nothing to execute"
			}
		}
	}

	private static String replaceParameters(File sqlFile, Map<String, String> parameters){
		String newSqlFile = sqlFile.text;
		if (parameters != null) {
			for (parameter in parameters) {
				newSqlFile = newSqlFile.replaceAll(parameter.key, parameter.value);
			}
		}
		return newSqlFile;
	}

	public static Object getId(File feature, String dbVendor, String fileExtension, Object it, groovy.sql.Sql sql){
		def id = null;
		sql.eachRow(getSqlContent(feature, dbVendor, fileExtension)
				.replaceAll(":tenantId", String.valueOf(it))) { row ->
					id = row[0]
				}
		return id;
	}

	public static List<Object> getTenantsId(File feature, String dbVendor, groovy.sql.Sql sql){
		def tenants = []

		sql.query(getSqlContent(feature, dbVendor, "tenants")) { ResultSet rs ->
			while (rs.next()) tenants.add(rs.getLong(1));
		}
		return tenants;
	}

	public static String getSqlContent(File feature, String dbVendor, String suffix){
		return getSqlFile(feature, dbVendor, suffix).text.replaceAll(REQUEST_SEPARATOR, "").replaceAll("\r\n", "\n");
	}
	
	private static File getSqlFile(File folder, String dbVendor, String suffix){
		return new File(folder, dbVendor + (suffix == null || suffix.isEmpty() ? "" : "-" + suffix) + ".sql");
	}
}
