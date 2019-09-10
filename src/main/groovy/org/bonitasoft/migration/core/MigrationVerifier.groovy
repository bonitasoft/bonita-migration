package org.bonitasoft.migration.core

class MigrationVerifier implements MigrationAction {

    List<VersionMigration> migrationVersions
    MigrationContext context
    Logger logger
    DisplayUtil displayUtil

    @Override
    void run(boolean isSp) {
        def warnings
        def errors
        def lastPossibleVersion
        (errors, lastPossibleVersion) = getBlockingPrerequisites()
        warnings = getWarningPrerequisites()
        if (!errors.isEmpty()) {
            logger.error("Migration to version ${migrationVersions.last().version} is currently not possible" +
                    "${lastPossibleVersion ? ", you can only migrate to version " + lastPossibleVersion : ""}:")
            errors.each {
                logger.error(" * Step ${it.key}:")
                it.value.each { message ->
                    logger.error(message)
                }
            }
        } else if (!warnings.isEmpty()) {
            logger.warn("Migration to version ${migrationVersions.last().version} is possible but there is some warnings:")
            warnings.each {
                logger.warn(" * Step ${it.key}:")
                it.value.each { message ->
                    logger.warn(message)
                }
            }
        } else {
            logger.info("Migration to version ${migrationVersions.last().version} is possible.")
        }
    }


    def getBlockingPrerequisites() {
        Map<String, String[]> beforeMigrationBlocks = [:]
        def hasBlockings = false
        def lastPossibleVersion = null
        migrationVersions.each {
            VersionMigration versionMigration ->
                String[] preVersionBlockings = versionMigration.getPreMigrationBlockingMessages(context)
                if (preVersionBlockings) {
                    beforeMigrationBlocks.put(versionMigration.version, preVersionBlockings)
                    hasBlockings = true
                } else if (!hasBlockings) {
                    lastPossibleVersion = versionMigration.version
                }
        }
        [beforeMigrationBlocks, lastPossibleVersion]
    }

    Map<String, String[]> getWarningPrerequisites() {
        Map<String, String[]> beforeMigrationWarnings = [:]
        migrationVersions.each {
            // Warn before running ANY migration step if there are pre-migration warnings:
            String[] preVersionWarnings = it.getPreMigrationWarnings(context)
            if (preVersionWarnings) {
                beforeMigrationWarnings.put(it.version, preVersionWarnings)
            }
        }
        beforeMigrationWarnings
    }

    @Override
    List<String> getBannerAndGlobalWarnings() {
        return [
                "The migration tool was executed using '--verify' option, it will verify if the migration is possible.",
                "NO CHANGES WILL BE MADE.",
                "",
                "Remove the '--verify' option to actually run the migration."]
    }

    @Override
    String getDescription() {
        return  "NO CHANGES WILL BE MADE."
    }
}
