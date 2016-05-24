/*
 * Copyright (C) 2016 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */
package org.bonitasoft.migration.version.to7_3_0

import oracle.sql.BLOB
import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

/**
 * @author Laurent Leseigneur
 */
class MigrateAvatarIT extends Specification {

    @Shared
    Logger logger = new Logger()

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    @Rule
    TemporaryFolder temporaryFolder

    def setup() {
        migrationContext.setVersion("7.3.0")
        dropTables()
        dbUnitHelper.createTables("7_3_0/icon", "user")
        migrationContext.bonitaHome = temporaryFolder.newFolder()

    }

    def cleanup() {
        dropTables()
    }

    private String[] dropTables() {
        dbUnitHelper.dropTables(["icon", "user_", "sequence"] as String[])
    }

    def "should insert avatar into icon table "() {
        given:
        def tenant12Folder = createTenantDir(12L)
        tenant12Folder.resolve("tmp_58693177498535435.jpeg.jpeg").toFile().setBytes([1, 2, 3] as byte[])
        insertUser(12L, 45L, "john", "tmp_58693177498535435.jpeg.jpeg")
        writeIcon(tenant12Folder, "avatar3307049340281126580.gif", "tenant 12 avatar of jack")
        insertUser(12L, 46L, "jack", "avatar3307049340281126580.gif")
        writeIcon(tenant12Folder, "avatar4407045230281126580.png", "tenant 12 avatar of james")
        insertUser(12L, 47L, "james", "avatar4407045230281126580.png")
        insertUser(12L, 48L, "anOther", "noExisting.png")
        def tenant13Folder = createTenantDir(13L)
        writeIcon(tenant13Folder, "avatar4407045230281126580.jpg", "tenant 13 avatar of john")
        insertUser(13L, 45L, "john", "avatar4407045230281126580.jpg")
        writeIcon(tenant13Folder, "avatar5507045230281126580.jpg", "tenant 13 avatar of jack")
        insertUser(13L, 46L, "jack", "avatar5507045230281126580.jpg")
        when:
        new MigrateAvatar().execute(migrationContext)
        then:
        assert getIconOf("john", 12L) == [tenantid: 12L, mimetype: "image/jpeg", content: [1, 2, 3] as byte[]]
        assert getIconOf("jack", 12L) == [tenantid: 12L, mimetype: "image/gif", content: "tenant 12 avatar of jack".bytes]
        assert getIconOf("james", 12L) == [tenantid: 12L, mimetype: "image/png", content: "tenant 12 avatar of james".bytes]
        assert getIconOf("john", 13L) == [tenantid: 13L, mimetype: "image/jpeg", content: "tenant 13 avatar of john".bytes]
        assert getIconOf("jack", 13L) == [tenantid: 13L, mimetype: "image/jpeg", content: "tenant 13 avatar of jack".bytes]
        assert getIconOf("anOther", 12L) == null
        assert migrationContext.sql.firstRow("SELECT nextid FROM sequence WHERE tenantid = ${12L} AND id = ${27L}").nextid == 4
        assert migrationContext.sql.firstRow("SELECT nextid FROM sequence WHERE tenantid = ${13L} AND id = ${27L}").nextid == 3

    }

    protected insertUser(Long tenantId, Long userId, String username, String iconName) {
        migrationContext.sql.execute("INSERT INTO user_ (tenantid, id, enabled, userName, iconPath) VALUES (${tenantId},${userId}, ${true}, ${username}, ${("/users/" + iconName)})")
    }

    protected writeIcon(Path tenant12Folder, String name, String content) {
        tenant12Folder.resolve(name).toFile().write(content)
    }

    Path createTenantDir(long tenantId) {
        def tenant12Folder
        tenant12Folder = migrationContext.bonitaHome.toPath().resolve("client").resolve("tenants").resolve(String.valueOf(tenantId)).resolve("work").resolve("icons").resolve("users")
        Files.createDirectories(tenant12Folder)
        tenant12Folder
    }

    protected getIconOf(username, tenantId) {

        Map map = migrationContext.sql.firstRow("""SELECT icon.tenantid, icon.mimetype, icon.content FROM icon, user_
WHERE user_.iconid = icon.id AND user_.tenantid = icon.tenantid AND user_.userName = ${
            username
        } AND user_.tenantid = ${tenantId}""") as Map
        if (map == null) {
            return null
        }
        if (MigrationStep.DBVendor.ORACLE.equals(migrationContext.dbVendor)) {
            map.put("content", ((BLOB) map.get("content")).binaryStream.bytes)
        }
        map.collectEntries { it -> [it.key.toString().toLowerCase(), it.value] }
    }


}
