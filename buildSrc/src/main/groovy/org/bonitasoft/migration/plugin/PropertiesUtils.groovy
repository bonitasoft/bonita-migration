/**
 * Copyright (C) 2018 Bonitasoft S.A.
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

package org.bonitasoft.migration.plugin

import org.gradle.api.Project

class PropertiesUtils {

    public static final String JAVA_8_BIN = "JAVA_8_BIN"

    static def loadProperties(File propertiesFile) {
        def props = new Properties()
        propertiesFile.withInputStream {
            props.load(it)
        }
        return props
    }

    static Properties loadProperties(InputStream stream) {
        new Properties().with { p ->
            stream.withStream { load(it) }
            return p
        }
    }

    static String getJava8Binary(Project project, String taskName) {
        if (project.hasProperty(JAVA_8_BIN)) {
            def java8 = project.property(JAVA_8_BIN)
            project.logger.info("Running task ${taskName} with JVM '$java8'")
            return java8
        } else {
            throw new IllegalStateException("Task ${taskName} must be run with Java 8, run the build with property '-P$JAVA_8_BIN=<path to java 8 binary>'")
        }
    }

}
