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
package org.bonitasoft.migration.version.to7_3_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

import javax.activation.MimetypesFileTypeMap
import java.nio.file.Files

/**
 *
 * @author Laurent Leseigneur
 */
class MigrateAvatar extends MigrationStep {


    private static final MimetypesFileTypeMap MIMETYPES_FILE_TYPE_MAP = new MimetypesFileTypeMap();

    @Override
    def execute(MigrationContext context) {
        MIMETYPES_FILE_TYPE_MAP.addMimeTypes("image/png png PNG");
        MIMETYPES_FILE_TYPE_MAP.addMimeTypes("image/gif gif GIF");
        MIMETYPES_FILE_TYPE_MAP.addMimeTypes("image/jpeg jpeg jpg jpe JPG");
        def helper = context.databaseHelper
        helper.executeScript("icon", "icon")
        migrateUserIcons(context)
        helper.executeScript("icon", "user")
    }


    @Override
    String getDescription() {
        return "Migrate user, group and role icons in database"
    }

    def migrateUserIcons(MigrationContext context) {
        //migrate user icons
        def Map<Long, Long> ids = [:]
        context.databaseHelper.sql.eachRow("SELECT u.tenantid, u.iconpath, u.id FROM user_ u WHERE u.iconpath IS NOT null") { row ->

            def tenantId = String.valueOf(row.tenantid)
            def icon = context.bonitaHome.toPath().resolve("client").resolve("tenants").resolve(tenantId).resolve("work").resolve("icons").resolve(row.iconpath.substring(1))
            if (!Files.isReadable(icon)) {
                context.logger.info "user icon ${icon} does not exists in file system. Skip icon migration"
                return
            }
            context.logger.info "store user icon ${icon} in database"
            def iconId = getNextId(ids, Long.valueOf(tenantId))
            context.sql.executeInsert("INSERT INTO icon (tenantid, id, mimetype, content) VALUES (${row.tenantid}, ${iconId}, ${MIMETYPES_FILE_TYPE_MAP.getContentType(icon.toFile()) ?: "image/png"}, ${icon.bytes})")
            context.sql.executeUpdate("UPDATE user_ SET iconid = ${iconId} WHERE user_.tenantid = ${row.tenantid} AND user_.id = ${row.id}")
        }
        context.databaseHelper.insertSequences(ids, context, 27)

    }

    def getNextId(Map map, long tenantId) {
        if (!map.containsKey(tenantId)) {
            map.put(tenantId, 1)
        }
        map.put(tenantId, map.get(tenantId) + 1)
    }
}
