/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.update.core

import com.github.zafarkhaja.semver.Version
import groovy.sql.Sql
import groovy.time.TimeCategory
import org.bonitasoft.update.core.UpdateStep.DBVendor

import java.sql.ResultSet

import static org.bonitasoft.update.core.UpdateStep.DBVendor.*

/**
 *
 * Util classes that contains common methods for update
 *
 * @author Baptiste Mesta
 * @author Celine Souchet
 *
 */
class UpdateUtil {

    public final static String AUTO_ACCEPT = "auto.accept"

    public final static String REQUEST_SEPARATOR = "@@"

    public static read = System.in.newReader().&readLine

    static boolean isAutoAccept() {
        return System.getProperty(AUTO_ACCEPT) == "true"
    }

    private static final Logger logger = new Logger()

    static logSuccessUpdate(Date startFeatureDate, Date startUpdateDate) {
        if (startFeatureDate == null || startUpdateDate == null) {
            throw new IllegalArgumentException("Can't execute logSuccessUpdate method with arguments : startFeatureDate = " + startFeatureDate + ", startUpdateDate = " + startUpdateDate)
        }

        def endFeatureDate = new Date()
        logger.info("| --> Update step success in " + TimeCategory.minus(endFeatureDate, startFeatureDate) + ". Update started " + TimeCategory.minus(endFeatureDate, startUpdateDate) + " ago. --")
    }

    static Sql getSqlConnection(String dburl, String user, String pwd, String driverClass) {
        return Sql.newInstance(dburl, user, pwd, driverClass)
    }

    static executeDefaultSqlFile(File file, String dbVendor, Sql sql) {
        executeSqlFile(file, dbVendor, null, null, sql, true)
    }

    /**
     * execute a sql file
     * @param feature
     *      the folder where the feature to update is
     * @param dbVendor
     *      the current database type
     * @param suffix
     *      the suffix of the file to execute, format is: <dbVendor>-<suffixe>.sql
     * @param parameters
     *      parameters to replace inside the file, if parameter 'tenant' is given, all word 'tenant' will be replace by the value
     * @param sql
     *      the sql connection to the database
     * @param toUpdate
     *      if this will update elements
     */

    static executeSqlFile(File feature, String dbVendor, String suffix, Map<String, String> parameters, Sql sql, boolean toUpdate) {
        def sqlFile = getSqlFile(feature, dbVendor, suffix)
        if (sqlFile.exists()) {
            def List<String> contents = getSqlContent(sqlFile.text, parameters)

            for (content in contents) {
                if (!content.trim().empty) {
                    if (toUpdate) {
                        def count = sql.executeUpdate(content)
                        if (count > 0) {
                            logger.info count + " row(s) updated"
                        }
                    } else {
                        sql.execute(content)
                    }
                }
            }
        } else {
            logger.info "nothing to execute"
        }
    }

    static List<String> getSqlContent(String sqlFileContent, Map<String, String> parameters) {
        def sqlFileContentWithParameters = replaceParameters(sqlFileContent, parameters).replaceAll("\r", "")
        return Arrays.asList(sqlFileContentWithParameters.split(REQUEST_SEPARATOR))
    }

    static File getSqlFile(File folder, String dbVendor, String suffix) {
        if (folder == null || dbVendor == null || "".equals(dbVendor)) {
            throw new IllegalArgumentException("Can't execute getSqlFile method with arguments : folder = " + folder + ", dbVendor = " + dbVendor)
        }
        return new File(folder, dbVendor + (suffix == null || suffix.isEmpty() ? "" : "-" + suffix) + ".sql")
    }

    static String replaceParameters(String sqlFileContent, Map<String, String> parameters) {
        if (sqlFileContent == null) {
            throw new IllegalArgumentException("Can't execute replaceParameters method with arguments : sqlFileContent = " + sqlFileContent)
        }

        String newSqlFileContent = sqlFileContent
        if (parameters != null) {
            for (parameter in parameters) {
                newSqlFileContent = newSqlFileContent.replaceAll(parameter.key, String.valueOf(parameter.value))
            }
        }
        return newSqlFileContent
    }

    static Version getPlatformVersion(Sql sql) {
        def row = sql.firstRow("SELECT version FROM platform")
        def versionAsString = row[0] as String

        // check if the version follows the 7.11+ version pattern in database:
        def matcherFor711plus = (versionAsString =~ /(\d+)\.(\d+)/)
        if (matcherFor711plus.matches()) {
            // Only need the first 2 digits, as default patch number is already '0':
            return Version.forIntegers(matcherFor711plus[0][1] as int, matcherFor711plus[0][2] as int)
        } else {
            def fullVersion = Version.valueOf(versionAsString)
            // Ignore last digit because we don't update maintenance versions of 7.10.x anymore, we directly go to 7.11
            return Version.forIntegers(fullVersion.majorVersion, fullVersion.minorVersion)
        }
    }

    static String getDisplayVersion(def normalVersion) {
        Version semVer
        if (normalVersion instanceof Version) {
            semVer = normalVersion
        } else {
            semVer = Version.valueOf(normalVersion)
        }
        return semVer.majorVersion + '.' + semVer.minorVersion
    }

    static Object getId(File feature, String dbVendor, String fileExtension, Object it, Sql sql) {
        if (it == null || sql == null) {
            throw new IllegalArgumentException("Can't execute getId method with arguments : it = " + it + ", sql = " + sql)
        }

        def sqlFile = getSqlFile(feature, dbVendor, fileExtension)
        def parameters = Collections.singletonMap(":tenantId", String.valueOf(it))
        def id = null
        sql.eachRow(getSqlContent(sqlFile.text, parameters).get(0)) { row ->
            id = row[0]
        }
        return id
    }

    /**
     * Return a list of ids. 
     * The SQL file to execute need to begin by "SELECT id FROM...".
     */

    static List<Long> getIds(File feature, String dbVendor, String fileExtension, Map<String, String> parameters, Sql sql) {
        if (sql == null) {
            throw new IllegalArgumentException("Can't execute getId method with arguments : sql = " + sql)
        }
        def sqlFile = getSqlFile(feature, dbVendor, fileExtension)
        def ids = []
        sql.query(getSqlContent(sqlFile.text, parameters).get(0)) { ResultSet rs ->
            while (rs.next())
                ids.add(rs.getLong(1))
        }
        return ids
    }

    static List<Object> getTenantIds(Sql sql) {
        if (sql == null) {
            throw new IllegalArgumentException("Can't execute getTenantIds method with arguments : sql = " + sql)
        }
        def tenants = []

        sql.query("SELECT id FROM tenant ORDER BY id ASC") { ResultSet rs ->
            while (rs.next()) tenants.add(rs.getLong(1))
        }
        return tenants
    }

    static updateDirectory(String fromDir, String toDir, boolean deleteOldDirectory) {
        if (fromDir == null || toDir == null) {
            throw new IllegalArgumentException("Can't execute updateDirectory method with arguments : fromDir = " + fromDir + ", toDir = " + toDir)
        }
        def fileFromDir = new File(fromDir)
        def fileToDir = new File(toDir)

        if (!fileFromDir.exists() || !fileFromDir.isDirectory()) {
            throw new IllegalStateException("Update failed. Source folder does not exist : " + fromDir)
        }

        if (!deleteOldDirectory) {
            logger.info " | Adding/overwriting content in $toDir..."
        } else {
            IOUtil.deleteDirectory(fileToDir)
        }
        IOUtil.copyDirectory(fileFromDir, fileToDir)
    }

    static void askIfWeContinue() {
        if (!isAutoAccept()) {
            logger.info "Continue update? (yes/no): "
            String input = read()
            if (input != "yes") {
                logger.warn "Update cancelled"
                System.exit(0)
            }
        }
    }

    static String askForOptions(List<String> options) {
        def input = null
        while (true) {
            options.eachWithIndex { it, idx ->
                logger.info "${idx + 1} -- ${UpdateUtil.getDisplayVersion(it)} "
            }
            logger.info "choice: "
            input = read()
            try {
                def choiceNumber = Integer.valueOf(input)
                if (choiceNumber <= options.size()) {
                    //index in the list is -1, as arrays start at index 0:
                    String choice = options.get(choiceNumber - 1)
                    logger.info "$choiceNumber --> ${UpdateUtil.getDisplayVersion(choice)}"
                    return choice
                }
            } catch (Exception e) {
            }
            logger.warn "Invalid choice, please enter a value between 1 and ${options.size()}"
        }
    }

    static getNexIdsForTable(Sql sql, long sequenceId) {
        def idsByTenants = [:]
        sql.eachRow("SELECT tenantid,nextId from sequence WHERE id = $sequenceId") { row ->
            idsByTenants.put(row[0], row[1])
        }
        logger.info "next id by tenants for sequence id $sequenceId: $idsByTenants"
        return idsByTenants
    }

    static updateNextIdsForTable(Sql sql, long sequenceId, Map nexIdsByTenants) {
        nexIdsByTenants.each {
            logger.info "update next id to $it.value for sequence id $sequenceId on tenant $it.key"
            logger.info sql.executeUpdate("UPDATE sequence SET nextId = $it.value WHERE tenantId = $it.key and id = $sequenceId") + " row(s) updated"
        }
    }

    static List<String> getDatabaseInformation(Sql sql, DBVendor dbVendor) {
        def info = []

        try {
            switch (dbVendor) {
                case MYSQL:
                    sql.eachRow('SHOW VARIABLES LIKE "%version%"') { row ->
                        info << "${row[0]} ${row[1]}"
                    }
                    break
                case ORACLE:
                    sql.eachRow('SELECT * FROM v$version') { row ->
                        info << row[0]
                    }
                    break
                case POSTGRES:
                    sql.eachRow('SELECT version()') { row ->
                        info << row[0]
                    }
                    break
                case SQLSERVER:
                    def columnNames = ['ProductVersion', 'ProductLevel', 'ProductUpdateReference', 'Edition', 'EngineEdition', 'Collation', 'SqlCharSetName']
                    String queryElements = columnNames.collect { "SERVERPROPERTY('${it}') ${it}" }.join(', ')
                    String query = "select ${queryElements}"

                    sql.eachRow(query) { row ->
                        columnNames.each {
                            info << "${it} ${row[it]}"
                        }
                    }
                    break
            }
        } catch (Exception e) {
            logger.info("Unable to get database information: ${e.getMessage()}")
            logger.debug('Details', e)
        }
        return info
    }

}

