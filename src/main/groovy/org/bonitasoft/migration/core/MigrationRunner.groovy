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
class MigrationRunner implements MigrationAction {

    public static final String VERSION_OVERRIDDEN = "version.overridden"
    public static final String VERSION_OVERRIDE_BY = "version.override.by"
    List<VersionMigration> migrationVersions
    MigrationContext context
    Logger logger
    DisplayUtil displayUtil
    def overriddenVersion
    def overrideByVersion

    @Override
    void run(boolean isSp) {

        if (!hasBlockingPrerequisites()) {
            getWarningPrerequisites()

            Date migrationStartDate = new Date()
            String lastVersion
            def warnings = [:]
            migrationVersions.each {
                logger.info "Execute migration to version " + MigrationUtil.getDisplayVersion(it.getVersion())
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
                        warnings.put("Migration to version:${MigrationUtil.getDisplayVersion(it.version)} - step: ${step.description}" as String, stepWarningMessage)
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
            String postMigrationWarnings = this.postMigrationWarnings()
            if (postMigrationWarnings) {
                warnings.put("Global post-migration warning", postMigrationWarnings)
            }
            if (warnings) {
                logger.warn " However, some warnings require your attention:"
                warnings.each { line ->
                    displayUtil.logWarningsInRectangleWithTitle(line.key, line.value.split("\n"))
                }
            }
        }
    }


    private String postMigrationWarnings() {
        if (context.databaseHelper.hasTable("arch_contract_data_backup")) {
            return "Archive contract data table backup had been created (\"arch_contract_data_backup\") as its model migration is time consuming.\n" +
                    "All this information is not required by Bonita to work and does not affect user experience,\n" +
                    "but it keeps the information of all contracts sent to execute tasks or instantiate processes.\n" +
                    "Based on your needs, this information can be migrated into the original table using the tool\n" +
                    "(please run live-migration script located in tools/live-migration directory) while bonita platform is up & running\n" +
                    "or dropped to reduce disk space"
        }
        return null

    }

    @Override
    List<String> getBannerAndGlobalWarnings() {
        return [
                "This tool will migrate your installation of Bonita.",
                "Both database and bonita home will be modified.",
                "Please refer to the documentation for further steps to completely migrate your production environment.",
                "",
                "Warning:",
                "Back up the database AND the bonita home before migrating",
                "If you have a custom Look & Feel, test and update it, if it's necessary when the migration is finished.",
                "If you have customized the configuration of your bonita home, reapply the customizations when the migration is finished.", ""]
    }

    @Override
    String getDescription() {
        return "DATABASE WILL BE MIGRATED (use --verify to run only checks)"
    }

    private boolean hasBlockingPrerequisites() {
        Map<String, String[]> beforeMigrationBlocks = [:]
        migrationVersions.each {
            VersionMigration versionMigration ->
                String[] preVersionBlockings = versionMigration.getPreMigrationBlockingMessages(context)
                if (preVersionBlockings) {
                    beforeMigrationBlocks.put("Migration to version ${MigrationUtil.getDisplayVersion(versionMigration.getVersion())}", preVersionBlockings)
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

    private void getWarningPrerequisites() {
        checkOverrideValidity()
        Map<String, String[]> beforeMigrationWarnings = [:]
        migrationVersions.each {
            // Warn before running ANY migration step if there are pre-migration warnings:
            String[] preVersionWarnings = it.getPreMigrationWarnings(context)
            if (preVersionWarnings) {
                beforeMigrationWarnings.put("Migration to version ${MigrationUtil.getDisplayVersion(it.getVersion())}", preVersionWarnings)
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
        if (Version.valueOf(lastVersion) < Version.valueOf("7.11.0")) {
            logger.info(" The version of your Bonita installation is now: $lastVersion")
        } else {
            logger.info(" The version of your Bonita database schema is now: ${MigrationUtil.getDisplayVersion(lastVersion)}")
        }
        if (Version.valueOf(lastVersion) < Version.valueOf("7.3.0")) {
            logger.info(" Now, you must reapply the customizations of your bonita home.")
        }
        logger.info("--------------------------------------------------------------------------------------")
    }

    def changePlatformVersion(Sql sql, String version) {
        def targetVersion = Version.valueOf(version)
        if (targetVersion < Version.valueOf("7.11.0")) {
            if (overriddenVersion && overrideByVersion && overriddenVersion == version) {
                logger.info("Overriding version $version by $overrideByVersion")
                version = overrideByVersion
            }
            logger.info("Updating platform version in the database ...")
            sql.executeUpdate("UPDATE platform SET previousVersion = version, version = $version")
            logger.info("Platform version in database is now $version")
        } else {
            String dbVersion = "${targetVersion.majorVersion}.${targetVersion.minorVersion}"
            logger.info("Updating Bonita database schema version to $dbVersion")
            sql.executeUpdate("UPDATE platform SET version = $dbVersion")
        }
    }

}
