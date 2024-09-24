/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to7_11_0

import org.bonitasoft.update.core.UpdateStep
import org.bonitasoft.update.core.VersionUpdate

class UpdateTo7_11_0 extends VersionUpdate {

    @Override
    List<UpdateStep> getUpdateSteps() {
        return [
            new AddIndexOnArchFlownodeInstance(),
            new RefactorPlatformColumns(),
            new UpdateBOM(),
            new AddIndexOnProcessComments()
        ]
    }
}
