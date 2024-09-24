/**
 * Copyright (C) 2024 Bonitasoft S.A.
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

class UpdateVerifier implements UpdateAction {

    List<VersionUpdate> versionUpdates
    UpdateContext context
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
            logger.error("Update to version ${versionUpdates.last().version} is currently not possible" +
                    "${lastPossibleVersion ? ", you can only update to version " + lastPossibleVersion : ""}:")
            errors.each {
                logger.error(" * Step ${it.key}:")
                it.value.each { message ->
                    logger.error(message)
                }
            }
        } else if (!warnings.isEmpty()) {
            logger.warn("Update to version ${versionUpdates.last().version} is possible but there is some warnings:")
            warnings.each {
                logger.warn(" * Step ${it.key}:")
                it.value.each { message ->
                    logger.warn(message)
                }
            }
        } else {
            logger.info("Update to version ${versionUpdates.last().version} is possible.")
        }
    }


    def getBlockingPrerequisites() {
        Map<String, String[]> beforeUpdateBlocks = [:]
        def hasBlockings = false
        def lastPossibleVersion = null
        versionUpdates.each { VersionUpdate versionUpdate ->
            String[] preVersionBlockings = versionUpdate.getPreUpdateBlockingMessages(context)
            if (preVersionBlockings) {
                beforeUpdateBlocks.put(versionUpdate.version, preVersionBlockings)
                hasBlockings = true
            } else if (!hasBlockings) {
                lastPossibleVersion = versionUpdate.version
            }
        }
        [beforeUpdateBlocks, lastPossibleVersion]
    }

    Map<String, String[]> getWarningPrerequisites() {
        Map<String, String[]> updateWarnings = [:]
        versionUpdates.each {
            // Warn before running ANY update step if there are pre-update warnings:
            String[] preVersionWarnings = it.getPreUpdateWarnings(context)
            if (preVersionWarnings) {
                updateWarnings.put(it.version, preVersionWarnings)
            }
            String[] postVersionWarnings = it.getPostUpdateWarnings(context)
            if (postVersionWarnings) {
                updateWarnings.put(it.version, postVersionWarnings)
            }
        }
        updateWarnings
    }

    @Override
    List<String> getBannerAndGlobalWarnings() {
        return [
            "The update tool was executed using '--verify' option, it will verify if the update is possible.",
            "NO CHANGES WILL BE MADE.",
            "",
            "Remove the '--verify' option to actually run the update."
        ]
    }

    @Override
    String getDescription() {
        return "NO CHANGES WILL BE MADE."
    }
}
