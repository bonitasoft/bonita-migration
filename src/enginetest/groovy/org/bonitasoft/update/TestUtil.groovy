/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
package org.bonitasoft.update

import groovy.sql.Sql

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * @author Baptiste Mesta
 */
class TestUtil {

    static Sql connection

    static byte[] createTestPageContent(String pageName, String displayName, String description) throws Exception {
        ByteArrayOutputStream e = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(e);
        zos.putNextEntry(new ZipEntry("Index.groovy"));
        zos.write("return \"\";".getBytes());
        zos.putNextEntry(new ZipEntry("page.properties"));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("name=");
        stringBuilder.append(pageName);
        stringBuilder.append("\n");
        stringBuilder.append("displayName=");
        stringBuilder.append(displayName);
        stringBuilder.append("\n");
        stringBuilder.append("description=");
        stringBuilder.append(description);
        stringBuilder.append("\n");
        zos.write(stringBuilder.toString().getBytes());
        zos.closeEntry();
        return e.toByteArray();
    }


    static getSql() {
        if (!connection) {
            def dburl = System.getProperty("db.url")
            def dbDriverClassName = System.getProperty("db.driverClass")
            def dbUser = System.getProperty("db.user")
            def dbPassword = System.getProperty("db.password")
            connection = Sql.newInstance(dburl, dbUser, dbPassword, dbDriverClassName)
        }
        connection
    }

    static boolean hasTable(String tableName) {
        def query

        def dbVendor = System.getProperty("db.vendor")
        switch (dbVendor) {
            case "postgres":
                query = """
                    SELECT *
                     FROM information_schema.tables
                     WHERE table_schema='public'
                       AND table_type='BASE TABLE'
                       AND UPPER(table_name) = UPPER($tableName)
                    """
                break

            case "oracle":
                query = """
                    SELECT *
                    FROM user_tables
                    WHERE UPPER(table_name) = UPPER($tableName)
                    """
                break

            case "mysql":
                query = """
                    SELECT *
                    FROM information_schema.tables
                    WHERE UPPER(table_name) = UPPER($tableName)
                    AND table_schema = DATABASE()
                    """
                break

            case "sqlserver":
                query = """
                    SELECT * FROM information_schema.tables
                    WHERE UPPER(TABLE_NAME) = UPPER($tableName)
                    """
                break
            default:
                throw new IllegalStateException("db vendor invalid: $dbVendor")
        }
        def firstRow = sql.firstRow(query)
        return firstRow != null
    }
}
