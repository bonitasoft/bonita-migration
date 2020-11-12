package org.bonitasoft.migration.plugin

import com.github.zafarkhaja.semver.Version
import org.gradle.api.Project

import static org.bonitasoft.migration.plugin.PropertiesUtils.loadProperties

/**
 * @author Baptiste Mesta.
 */
class VersionUtils {

    /**
     * From version 7.11.0, there is no migration step for patch versions (7.11.1, 7.11.2...)
     * For that reason, we need a way to allow to use a later patch version when pulling Bonita Engine to run tests.
     * This is done by defining an overriding version for each 7.11.0, 7.12.0 versions.
     * These overriding versions are defined in a file named 'bonita.test.versions' following the format:
     * ```
     * 7.11.0=7.11.3
     * 7.12.0=7.12.1
     * ...
     * ```
     */
    static Properties testProperties = loadProperties(VersionUtils.class.getResourceAsStream('/bonita.test.versions') as InputStream)

    static String dotted(String version) {
        return version.replace('_', '.')
    }

    static String underscored(String version) {
        return version.replace('.', '_')
    }

    static Version semver(String version) {
        return Version.valueOf(version.replace('_', '.'))
    }

    static List<String> getVersionList(project, configuration) {
        // use normalize to ensure the file content has only LF eol which is then used to split the lines (ex: manage
        // Windows CRLF checkout)
        project.file(configuration.versionListFile).text.normalize().split("\n").toList()
    }

    static String getVersion(Project project, MigrationPluginExtension configuration, String version) {
        def versionList = getVersionList(project, configuration)
        return getVersion(versionList, version, configuration)
    }

    static String getVersion(List<String> versions, String version, MigrationPluginExtension configuration) {
        def isLastVersion = (versions.last() == version)
        version = convertVersionForTests(version)
        if (isLastVersion) {
            if (configuration.currentVersionModifier != "NONE") {
                if (configuration.currentVersionModifier == "SNAPSHOT") {
                    return version + "-SNAPSHOT"
                } else {
                    //alpha, beta, rc tags have a dot here
                    return version + "." + configuration.currentVersionModifier
                }
            }
        }
        return version
    }

    static String convertVersionForTests(version) { testProperties.get(version, version) }

    static String getVersionBefore(List<String> previousVersions, String targetVersion) {
        for (int i = 0; i < previousVersions.size(); i++) {
            if (previousVersions.get(i).equals(targetVersion)) {
                return previousVersions.get(i - 1)
            }
        }
        if (Version.valueOf(targetVersion) > Version.valueOf(previousVersions.last())) {
            return previousVersions.last()
        }
        throw new IllegalStateException("no previous version for $targetVersion")
    }

}
