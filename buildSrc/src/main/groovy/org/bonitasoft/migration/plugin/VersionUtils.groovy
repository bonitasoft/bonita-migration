package org.bonitasoft.migration.plugin

import com.github.zafarkhaja.semver.Version
import org.gradle.api.Project

/**
 * @author Baptiste Mesta.
 */
class VersionUtils {

    static String dotted(String version) {
        return version.replace('_','.')
    }
    static String underscored(String version) {
        return version.replace('.','_')
    }
    static Version semver(String version) {
        return Version.valueOf(version.replace('_','.'))
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
        if (versions.last() == version) {
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
