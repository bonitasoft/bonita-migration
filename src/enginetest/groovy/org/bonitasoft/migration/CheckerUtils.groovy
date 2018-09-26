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

package org.bonitasoft.migration

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * @author Baptiste Mesta
 */
class CheckerUtils {

    /**
     * Initialize system properties to run the engine using properties from the Config.properties (migration properties)
     */
    public static void initializeEngineSystemProperties() {
        System.setProperty("sysprop.bonita.db.vendor", System.getProperty("db.vendor"));
        System.setProperty("sysprop.bonita.bdm.db.vendor", System.getProperty("db.vendor"));
        System.setProperty("db.url", System.getProperty("db.url"));
        System.setProperty("db.user", System.getProperty("db.user"));
        System.setProperty("db.password", System.getProperty("db.password"));
        def split = System.getProperty("db.url").split("/")
        def databaseName = split[split.length - 1]
        System.setProperty("db.database.name", databaseName);
    }


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
}
