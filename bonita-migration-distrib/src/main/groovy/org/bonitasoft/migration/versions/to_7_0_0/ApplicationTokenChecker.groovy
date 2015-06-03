/**
 * Copyright (C) 2015 BonitaSoft S.A.
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

package org.bonitasoft.migration.versions.to_7_0_0

import org.bonitasoft.migration.core.Reporter

/**
 * @author Elias Ricken de Medeiros
 */
class ApplicationTokenChecker {

    private Reporter reporter
    private GlobalApplicationsMessageBuilder applicationMessageBuilder

    ApplicationTokenChecker(
            final Reporter reporter,
            final GlobalApplicationsMessageBuilder applicationMessageBuilder) {
        this.reporter = reporter
        this.applicationMessageBuilder = applicationMessageBuilder
    }

    void processInvalidTokens() {
        handleWarning(applicationMessageBuilder.buildMessage())
    }

    private void handleWarning(String invalidAppTokensMsg) {
        if (!invalidAppTokensMsg?.isEmpty()) {
            reporter.addWarning(invalidAppTokensMsg)
        }
    }
}
