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
package org.bonitasoft.migration.plugin

import org.gradle.api.Project
import org.gradle.api.Task

/**
 * @author Emmanuel Duchastenier
 */
class AlternateJVMRunner {

    public final static String ALTERNATE_JVM = 'org.bonitasoft.migration.runtime.alternateJvm'

    static useAlternateJVMRunnerIfRequired(Project project, Task task) {
        if (project.hasProperty(ALTERNATE_JVM)) {
            def alternateJvm = project.property(ALTERNATE_JVM)
            project.logger.info("Parameter '$ALTERNATE_JVM' detected. ${project.name} will use alternate JVM '$alternateJvm' to run $task")
            task.executable = alternateJvm
        }
    }
}
