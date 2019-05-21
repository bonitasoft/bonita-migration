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

    public static final String VERSION_OVERRIDDEN = "version.overridden"
    public static final String VERSION_OVERRIDE_BY = "version.override.by"
    List<VersionMigration> versionMigrations
    MigrationContext context
    Logger logger
    DisplayUtil displayUtil
    def overriddenVersion
    def overrideByVersion

    def run(boolean isSp) {

        if (!hasBlockingPrerequisites()) {
            checkPreWarningRequisites()

            Date migrationStartDate = new Date()
            String lastVersion
            def warnings = [:]
            versionMigrations.each {
                logger.info "Execute migration to version " + it.getVersion()
                def bonitaHomeDir = null
                it.context = context
                it.logger = logger
                context.setVersion(it.getVersion())
                if (Version.valueOf(it.getVersion()) < Version.valueOf("7.3.0")) {
                    bonitaHomeDir = it.migrateBonitaHome(isSp)
                }

                it.getMigrationSteps().each { step ->
                    logger.info "---------------"
                    logger.info "| Execute migration step: " + step.description
                    Date stepStartDate = new Date()
                    step.execute(context)
                    def stepWarningMessage = step.warning
                    if (stepWarningMessage) {
                        warnings.put("migration to version:${it.version} - step:${step.description}", stepWarningMessage)
                    }
                    MigrationUtil.logSuccessMigration(stepStartDate, migrationStartDate)
                    logger.info "---------------"
                }
                changePlatformVersion(context.sql, it.getVersion())
                lastVersion = it.getVersion()

                if (bonitaHomeDir) {
                    logger.debug("Removing entire content of bonita-home folder $bonitaHomeDir")
                    bonitaHomeDir.deleteDir()
                }
            }
            logSuccessfullyCompleted(migrationStartDate, lastVersion)
            if (warnings) {
                logger.warn " However, some warnings require your attention:"
                warnings.each { line ->
                    displayUtil.logWarningsInRectangleWithTitle(line.key, line.value.split("\n"))
                }
            }
        }
    }

    private boolean hasBlockingPrerequisites() {
        Map<String, String[]> beforeMigrationBlocks = [:]
        versionMigrations.each {
            VersionMigration versionMigration ->
                String[] preVersionBlockings = versionMigration.getPreMigrationBlockingMessages(context)
                if (preVersionBlockings) {
                    beforeMigrationBlocks.put("Migration to version ${versionMigration.getVersion()}", preVersionBlockings)
                }
        }
        if (beforeMigrationBlocks) {
            logger.warn "Some migration steps cannot complete :"
            beforeMigrationBlocks.each { warning ->
                displayUtil.logWarningsInRectangleWithTitle(warning.key, warning.value)
            }
            return true
        }
        return false
    }

    private void checkPreWarningRequisites() {
        checkOverrideValidity()
        Map<String, String[]> beforeMigrationWarnings = [:]
        versionMigrations.each {
            // Warn before running ANY migration step if there are pre-migration warnings:
            String[] preVersionWarnings = it.getPreMigrationWarnings(context)
            if (preVersionWarnings) {
                beforeMigrationWarnings.put("Migration to version ${it.getVersion()}", preVersionWarnings)
            }
        }
        if (beforeMigrationWarnings) {
            logger.warn "Some migration steps have important pre-requisites:"
            beforeMigrationWarnings.each { warning ->
                displayUtil.logWarningsInRectangleWithTitle(warning.key, warning.value)
            }
            MigrationUtil.askIfWeContinue()
        }
    }

    def checkOverrideValidity() {
        overriddenVersion = System.getProperty(VERSION_OVERRIDDEN)
        overrideByVersion = System.getProperty(VERSION_OVERRIDE_BY)
        if (overriddenVersion && !overrideByVersion || !overriddenVersion && overrideByVersion) {
            logger.warn("System properties '$VERSION_OVERRIDDEN' and '$VERSION_OVERRIDE_BY' must be set together")
        }
    }

    private void logSuccessfullyCompleted(Date migrationStartDate, String lastVersion) {
        def end = new Date()
        logger.info("--------------------------------------------------------------------------------------")
        logger.info(" Migration successfully completed, in " + TimeCategory.minus(end, migrationStartDate))
        logger.info(" The version of your Bonita installation is now: $lastVersion")
        if (Version.valueOf(lastVersion) < Version.valueOf("7.3.0")) {
            logger.info(" Now, you must reapply the customizations of your bonita home.")
        }
        logger.info("--------------------------------------------------------------------------------------")
    }

    def changePlatformVersion(Sql sql, String version) {
        if (overriddenVersion && overrideByVersion && overriddenVersion == version) {
            logger.info("Overriding version $version by $overrideByVersion")
            version = overrideByVersion
        }
        logger.info("Updating platform version in the database ...")
        sql.executeUpdate("UPDATE platform SET previousVersion = version")
        sql.executeUpdate("UPDATE platform SET version = $version")
        logger.info("Platform version in database is now $version")
    }


}
