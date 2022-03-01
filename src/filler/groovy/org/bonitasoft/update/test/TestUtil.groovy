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
package org.bonitasoft.update.test

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * @author Baptiste Mesta
 */
class TestUtil {

    public
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
