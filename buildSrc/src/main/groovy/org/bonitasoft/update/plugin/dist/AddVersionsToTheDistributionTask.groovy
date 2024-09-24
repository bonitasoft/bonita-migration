/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
package org.bonitasoft.update.plugin.dist

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * @author Laurent Leseigneur
 */
class AddVersionsToTheDistributionTask extends DefaultTask {

    @Input
    String[] versionsToAdd = []
    @OutputFile
    File propertiesFile

    @Override
    @Internal
    String getDescription() {
        return "Add available versions in the configuration file of the distribution"
    }

    @TaskAction
    def addVersionsToTheDistribution() {
        Properties properties = new Properties()
        properties.put "versions", versionsToAdd.toString()
        if (!propertiesFile.exists()) {
            propertiesFile.parentFile.mkdirs()
            propertiesFile.createNewFile()
        }
        properties.store(new FileWriter(propertiesFile), "Bonita versions for update");

    }
}
