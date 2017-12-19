package org.bonitasoft.migration.plugin

import com.github.zafarkhaja.semver.Version

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

}
