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

import com.github.zafarkhaja.semver.Version
import groovy.sql.Sql
import groovy.time.TimeCategory

/**
 * @author Baptiste Mesta
 */
class MigrationRunner {

    List<VersionMigration> versionMigrations
    MigrationContext context
    Logger logger

    def run(boolean isSp) {
        Date migrationStartDate = new Date()
        String lastVersion
        def warnings = [:]
        versionMigrations.each {
            logger.info "Execute migration to version " + it.getVersion()
            it.context = context
            context.setVersion(it.getVersion())
            if (Version.valueOf(it.getVersion()) < Version.valueOf("7.3.0")) {
                it.migrateBonitaHome(isSp)
            }
            it.getMigrationSteps().each { step ->
                logger.info "---------------"
                logger.info "| Execute migration step: " + step.description
                Date stepStartDate = new Date()
                step.execute(context)
                def stepWarningMessage = step.warning
                if (stepWarningMessage != null) {
                    warnings.put("migration to version:${it.version} - step:${step.description}", stepWarningMessage)
                }
                MigrationUtil.printSuccessMigration(stepStartDate, migrationStartDate)
                logger.info "---------------"
            }
            changePlatformVersion(context.sql, it.getVersion())
            lastVersion = it.getVersion()
        }
        logSuccessfullyCompleted(migrationStartDate, lastVersion)
        if (!warnings.isEmpty()) {
            println "[WARNING] However, some warnings require your attention:"
            warnings.each {
                IOUtil.printInRectangleWithTitle(it.key, it.value.split("\n"))
            }
        }
    }

    private void logSuccessfullyCompleted(Date migrationStartDate, String lastVersion) {
        def end = new Date()
        logger.info("--------------------------------------------------------------------------------------")
        logger.info(" Migration successfully completed, in " + TimeCategory.minus(end, migrationStartDate))
        logger.info(" The version of your Bonita BPM installation is now: $lastVersion")
        if (Version.valueOf(lastVersion) < Version.valueOf("7.3.0")) {
            logger.info(" Now, you must reapply the customizations of your bonita home.")
        }
        logger.info("--------------------------------------------------------------------------------------")
    }

    def changePlatformVersion(Sql sql, String version) {
        logger.info("Updating platform version in the database ...")
        sql.executeUpdate("UPDATE platform SET previousVersion = version");
        sql.executeUpdate("UPDATE platform SET version = $version")
        logger.info("Platform version in database changed to $version")
    }

}
