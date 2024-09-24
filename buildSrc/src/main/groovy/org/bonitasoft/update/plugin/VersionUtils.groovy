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
package org.bonitasoft.update.plugin

import com.github.zafarkhaja.semver.Version

import static org.bonitasoft.update.plugin.PropertiesUtils.loadProperties
/**
 * @author Baptiste Mesta.
 */
class VersionUtils {

    /**
     * From version 7.11.0, there is no update step for patch versions (7.11.1, 7.11.2...)
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

    static String underscored(String version) {
        return version.replace('.', '_')
    }

    // Pad left with 0 if 1-digit, so that 9.0.0 comes BEFORE 10.0.0 --> 09_0_0 < 10_0_0
    static String padMajorVersionOn2Digits(String version) {
        def (major) = version.split('\\.')
        if (major.size() == 1) {
            return "0${version}"
        } else {
            return version
        }
    }

    static Version semver(String version) {
        return Version.valueOf(version.replace('_', '.'))
    }

    static List<String> getVersionList(project, configuration) {
        // use normalize to ensure the file content has only LF eol which is then used to split the lines (ex: manage
        // Windows CRLF checkout)
        project.file(configuration.versionListFile).text.normalize().split("\n").toList()
    }

    static List<String> getTestableVersionList(project, configuration) {
        return getVersionList(project, configuration)
    }

    static String getVersion(List<String> versions, String version, UpdatePluginExtension configuration) {
        def isLastVersion = (versions.last() == version)
        version = convertVersionForTests(version)
        if (isLastVersion) {
            if (configuration.currentVersionModifier != "NONE") {
                if (configuration.currentVersionModifier == "SNAPSHOT") {
                    // now, snapshot versions are 2-digits only:
                    return removeThirdDigit(version) + "-SNAPSHOT"
                } else {
                    //alpha, beta, rc tags have a dot here
                    return version + "." + configuration.currentVersionModifier
                }
            }
        }
        return version
    }

    static String removeThirdDigit(String version) {
        version.substring(0, version.lastIndexOf('.'))
    }

    static String convertVersionForTests(version) { testProperties.get(version, version) }

    static String getVersionBefore(List<String> previousVersions, String targetVersion) {
        for (int i = 0; i < previousVersions.size(); i++) {
            if (previousVersions.get(i) == targetVersion) {
                return previousVersions.get(i - 1)
            }
        }
        if (Version.valueOf(targetVersion) > Version.valueOf(previousVersions.last())) {
            return previousVersions.last()
        }
        throw new IllegalStateException("no previous version for $targetVersion")
    }

}
