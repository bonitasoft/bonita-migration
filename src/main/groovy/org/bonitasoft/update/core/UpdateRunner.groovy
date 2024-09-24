/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
 **/
package org.bonitasoft.update.core

import com.github.zafarkhaja.semver.Version
import groovy.sql.Sql
import groovy.time.TimeCategory

/**
 * @author Baptiste Mesta
 */
class UpdateRunner implements UpdateAction {

    public static final String VERSION_OVERRIDDEN = "version.overridden"
    public static final String VERSION_OVERRIDE_BY = "version.override.by"
    List<VersionUpdate> versionUpdates
    UpdateContext context
    Logger logger
    DisplayUtil displayUtil
    def overriddenVersion
    def overrideByVersion

    @Override
    void run(boolean isSp) {

        if (!hasBlockingPrerequisites()) {
            displayWarningPrerequisites()

            Date updateStartDate = new Date()
            String lastVersion = null
            def warnings = [:]
            versionUpdates.each {
                logger.info "Execute update to version " + UpdateUtil.getDisplayVersion(it.getVersion())
                it.context = context
                it.logger = logger
                context.setVersion(it.getVersion())

                it.getUpdateSteps().each { step ->
                    logger.info "---------------"
                    logger.info "| Execute update step: " + step.description
                    Date stepStartDate = new Date()
                    step.execute(context)
                    def stepWarningMessage = step.warning
                    if (stepWarningMessage) {
                        warnings.put("Update to version: ${UpdateUtil.getDisplayVersion(it.version)} - step: ${step.description}" as String, stepWarningMessage)
                    }
                    UpdateUtil.logSuccessUpdate(stepStartDate, updateStartDate)
                    logger.info "---------------"
                }

                String[] postUpdateWarnings = it.getPostUpdateWarnings(context)
                if (postUpdateWarnings) {
                    warnings.put("Post-update to version: ${UpdateUtil.getDisplayVersion(it.version)}" as String, postUpdateWarnings)
                }

                changePlatformVersion(context.sql, it.getVersion())
                lastVersion = it.getVersion()
            }

            logSuccessfullyCompleted(updateStartDate, lastVersion)

            String[] globalPostUpdateWarnings = this.getGlobalPostUpdateWarnings()
            if (globalPostUpdateWarnings) {
                warnings.put("Global post-update warnings", globalPostUpdateWarnings)
            }
            if (warnings) {
                logger.warn " However, some warnings require your attention:"
                warnings.each { line ->
                    displayUtil.logWarningsInRectangleWithTitle(line.key, line.value)
                }
            }
        }
    }

    private String[] getGlobalPostUpdateWarnings() {
        def warnings = []
        if (context.databaseHelper.hasTable("arch_contract_data_backup")) {
            warnings.addAll(UpdateUtil.ARCH_CONTRACT_DATA_BACKUP_GLOBAL_MSG)
        }
        return warnings
    }

    @Override
    List<String> getBannerAndGlobalWarnings() {
        return [
            "This tool will update the database of your installation of Bonita.",
            "Please refer to the documentation for further steps to completely update your production environment.",
            "",
            "Warning:",
            "Back up the database before updating",
            ""
        ]
    }

    @Override
    String getDescription() {
        return "DATABASE WILL BE UPDATED (use --verify to run only checks)"
    }

    private boolean hasBlockingPrerequisites() {
        Map<String, String[]> beforeUpdateBlocks = [:]
        versionUpdates.each { VersionUpdate versionUpdate ->
            String[] preVersionBlockings = versionUpdate.getPreUpdateBlockingMessages(context)
            if (preVersionBlockings) {
                beforeUpdateBlocks.put("Update to version ${UpdateUtil.getDisplayVersion(versionUpdate.getVersion())}" as String, preVersionBlockings)
            }
        }
        if (beforeUpdateBlocks) {
            logger.warn "Some update steps cannot complete:"
            beforeUpdateBlocks.each { warning ->
                displayUtil.logWarningsInRectangleWithTitle(warning.key, warning.value)
            }
            return true
        }
        return false
    }

    private void displayWarningPrerequisites() {
        checkOverrideValidity()
        Map<String, String[]> beforeUpdateWarnings = [:]
        versionUpdates.each {
            // Warn before running ANY update step if there are pre-update warnings:
            String[] preVersionWarnings = it.getPreUpdateWarnings(context)
            if (preVersionWarnings) {
                beforeUpdateWarnings.put("Update to version ${UpdateUtil.getDisplayVersion(it.getVersion())}" as String, preVersionWarnings)
            }
        }
        if (beforeUpdateWarnings) {
            logger.warn "Some update steps have important pre-requisites:"
            beforeUpdateWarnings.each { warning ->
                displayUtil.logWarningsInRectangleWithTitle(warning.key, warning.value)
            }
            UpdateUtil.askIfWeContinue()
        }
    }


    def checkOverrideValidity() {
        overriddenVersion = System.getProperty(VERSION_OVERRIDDEN)
        overrideByVersion = System.getProperty(VERSION_OVERRIDE_BY)
        if (overriddenVersion && !overrideByVersion || !overriddenVersion && overrideByVersion) {
            logger.warn("System properties '$VERSION_OVERRIDDEN' and '$VERSION_OVERRIDE_BY' must be set together")
        }
    }

    private void logSuccessfullyCompleted(Date updateStartDate, String lastVersion) {
        def end = new Date()
        logger.info("--------------------------------------------------------------------------------------")
        logger.info(" Update successfully completed, in " + TimeCategory.minus(end, updateStartDate))
        if (Version.valueOf(lastVersion) < Version.valueOf("7.11.0")) {
            logger.info(" The version of your Bonita installation is now: $lastVersion")
        } else {
            logger.info(" The version of your Bonita database schema is now: ${UpdateUtil.getDisplayVersion(lastVersion)}")
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
