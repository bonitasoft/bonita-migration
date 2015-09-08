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

package org.bonitasoft.migration.core

import groovy.sql.Sql

/**
 * @author Baptiste Mesta
 */
class MigrationRunner {

    List<VersionMigration> versionMigrations
    MigrationContext context
    Logger logger

    def run(boolean isSp) {
        context.openSqlConnection()
        versionMigrations.each {
            logger.info "Execute migration to version " + it.getVersion()
            it.context = context
            context.setVersion(it.getVersion())
            it.migrateBonitaHome(isSp)
            it.getMigrationSteps().each { step ->
                logger.info "---------------"
                logger.info "execute " + step.description
                step.execute(context)
            }
            changePlatformVersion(context.sql, it.getVersion())
        }
        context.closeSqlConnection()
    }

    def changePlatformVersion(Sql sql, String version) {
        sql.executeUpdate("UPDATE platform SET previousVersion = version");
        sql.executeUpdate("UPDATE platform SET version = $version")
        logger.info("Platform version in database changed to $version")
    }

}
