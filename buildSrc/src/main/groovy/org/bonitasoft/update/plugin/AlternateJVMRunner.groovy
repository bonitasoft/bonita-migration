/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.update.plugin

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import com.github.zafarkhaja.semver.Version

/**
 * @author Emmanuel Duchastenier
 */
class AlternateJVMRunner {

    // This feature is not used anymore in our tests / environment. It may be removed in the future:
    public final static String ALTERNATE_JVM = 'org.bonitasoft.update.runtime.alternateJvm'

    static useAlternateJVMRunnerIfRequired(Project project, Task task) {
        if (project.hasProperty(ALTERNATE_JVM)) {
            def alternateJvm = project.property(ALTERNATE_JVM)
            project.logger.info("Parameter '$ALTERNATE_JVM' detected. ${project.name} will use alternate JVM '$alternateJvm' to run $task")
            task.executable = alternateJvm
        }
    }

    static setupJavaToolChain(String version, Project project, TaskProvider taskProvider) {
        // Engine >= 10.0 requires Java 17 to run:
        if (Version.valueOf(version) >= Version.valueOf("10.0.0")) {
            JavaToolchainService service = project.getExtensions().getByType(JavaToolchainService.class);
            taskProvider.configure { javaLauncher.set(service.launcherFor { languageVersion = JavaLanguageVersion.of(17) }) }
        }
    }
}
