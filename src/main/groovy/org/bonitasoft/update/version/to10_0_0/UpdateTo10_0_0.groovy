/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to10_0_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep
import org.bonitasoft.update.core.VersionUpdate

/**
 * @author Emmanuel Duchastenier
 */
class UpdateTo10_0_0 extends VersionUpdate {

    public static final List<String> WARN_MESSAGE_JAVA_17 =
            ["Warning: Bonita versions 10.0.0 / 2024.1 and later only run on Java 17 environments.",
             "If your JRE or JDK is older than 17, you need to update your target environment before starting your updated Bonita platform."]

    @Override
    List<UpdateStep> getUpdateSteps() {
        // keep one line per step and comma (,) at start of line to avoid false-positive merge conflict:
        return [
        ]
    }

    @Override
    String[] getPreUpdateWarnings(UpdateContext context) {
        WARN_MESSAGE_JAVA_17
    }

}
