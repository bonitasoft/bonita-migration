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

import com.github.zafarkhaja.semver.Version

import static org.bonitasoft.update.core.UpdateUtil.getDisplayVersion

abstract class UpdateDefaultAction implements UpdateAction {

    // This list contains the steps in which we cannot stop.
    // Update will execute steps but slide until next version:
    // It's currently empty but add the version in the list if needed
    public static List<Version> TRANSITION_VERSIONS = ([] as String[]).collect {
        Version.valueOf(it)
    }

    Logger logger

    @Override
    void run(boolean isSp) {
        def versionUpdates = getVersionUpdatesToRun()
        if (versionUpdates.empty) {
            return
        }
        this.versionUpdates = versionUpdates
        runSpecificAction(isSp)
    }

    abstract void runSpecificAction(boolean isSp)
    abstract String getDescription()
    abstract List<VersionUpdate> getVersionUpdates()
    abstract void setVersionUpdates(List<VersionUpdate> versionUpdates)

    private Version getPlatformVersion() {
        return UpdateUtil.getPlatformVersion(context.sql)
    }

    List<VersionUpdate> getVersionUpdatesToRun() {
        def version = Version.valueOf(getPlatformVersion().normalVersion)
        logger.info("Detected version in database: ${getDisplayVersion(version)}")
        verifyPlatformIsValid(version)
        context.sourceVersion = version
        def versions = getVersionsAfter(version)
        def visibleVersions = filterOutInvisibleVersions(versions)
        if (visibleVersions.empty) {
            logger.warn("Your Bonita version is already the latest supported version. Nothing to update.")
            return []
        }
        logger.info(getDescription())
        if (context.targetVersion == null) {
            logger.info "Enter the target version"
            context.targetVersion = Version.valueOf(UpdateUtil.askForOptions(visibleVersions.collect {
                it.toString()
            }))
        }
        verifyTargetVersionIsValid(visibleVersions)
        return getVersionsToExecute(versions)
    }

    /**
     * get a version as string and return the class of the update step
     */
    Closure toVersionUpdateInstance = { Version it ->
        def versionUnderscored = it.toString().replace(".", "_")
        Class versionUpdateClass
        def className = "com.bonitasoft.update.version.to${versionUnderscored}.UpdateTo$versionUnderscored"
        try {
            logger.debug("Trying to find class " + className)
            versionUpdateClass = Thread.currentThread().contextClassLoader.loadClass(className)
        } catch (ClassNotFoundException ignored) {
            logger.debug("Unable to find Subscription specific class $className. Will use the Community version instead:")
            versionUpdateClass = Thread.currentThread().contextClassLoader.loadClass("org.bonitasoft.update.version.to${versionUnderscored}.UpdateTo$versionUnderscored")
        }
        logger.debug("Using class " + versionUpdateClass)
        return versionUpdateClass.newInstance(version: it, logger: logger)
    }

    static List<Version> filterOutInvisibleVersions(List<Version> versions) {
        versions.findAll { !TRANSITION_VERSIONS.contains(it) }
    }

    static def verifyPlatformIsValid(Version platformVersionInDatabase) {
        if (platformVersionInDatabase.majorVersion < 7
                || (platformVersionInDatabase.majorVersion == 7 && platformVersionInDatabase.minorVersion < 10)) {
            // Should we rename also migration tool v2 to update tool v2?
            throw new IllegalStateException("Sorry, but this tool can't manage version before 7.10.0, use the migration tool version 2")
        }
    }

    def verifyTargetVersionIsValid(List<Version> possibleTarget) {
        if (context.targetVersion < context.sourceVersion) {
            throw new IllegalStateException("The target version $context.targetVersion can not be before source version ${getDisplayVersion(context.sourceVersion)}")
        }
        if (context.targetVersion == context.sourceVersion) {
            throw new IllegalStateException("The version is already in ${getDisplayVersion(context.sourceVersion)}")
        }
        if (!possibleTarget?.contains(context.targetVersion)) {
            if (TRANSITION_VERSIONS.contains(context.targetVersion)) {
                if (System.getProperty("ignore.invalid.target.version") != null) {
                    // only accept this hidden sysprop "ignore.invalid.target.version" if the targetVersion is in the list of invisible transition versions:
                    logger.info("Ignoring normally-forbidden target version $context.targetVersion (for tests only)")
                } else {
                    throw new IllegalStateException("Updating to version $context.targetVersion is forbidden. Please choose a more recent version")
                }
            } else {
                throw new IllegalStateException("$context.targetVersion is not yet handled by this version of the update tool")
            }
        }
    }

    List<VersionUpdate> getVersionsToExecute(List<Version> versions) {
        return versions.subList(versions.indexOf(context.sourceVersion) + 1, versions.indexOf(context.targetVersion) + 1).collect(toVersionUpdateInstance) as List<VersionUpdate>
    }

    List<Version> getVersionsAfter(Version sourceVersion) {
        Properties updateProperties = getUpdateProperties()
        def versionsAsString = updateProperties.getProperty("versions")
        def allVersions = parseVersionsFromUpdateProperties(versionsAsString)
        def indexOfSourceVersion = allVersions.indexOf(sourceVersion)
        if (indexOfSourceVersion == -1) {
            throw new IllegalStateException("Sorry the version $sourceVersion can not be updated using this update tool")
        }
        return allVersions.subList(indexOfSourceVersion + 1, allVersions.size())
    }

    private static List<Version> parseVersionsFromUpdateProperties(String versionsAsString) {
        return versionsAsString.substring(1, versionsAsString.length() - 1).split(",").collect {
            it.trim()
        }.collect {
            Version.valueOf(it)
        }
    }

    private Properties getUpdateProperties() {
        return loadFromClasspath("/bonita-versions.properties")
    }


    private Properties loadFromClasspath(String name) {
        def properties = new Properties()
        this.class.getResourceAsStream(name).withStream {
            properties.load(it)
        }
        return properties
    }
}
