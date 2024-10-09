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
package org.bonitasoft.update.version.to10_2_0

import com.fasterxml.jackson.databind.ObjectMapper
import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

import static java.lang.System.*

/**
 * Update step to initialize the platform information in Community only
 * @author Emmanuel Duchastenier
 */
class InitPlatformInformation extends UpdateStep {

    static final int PERIOD_IN_DAYS = 30
    static final long PERIOD_IN_MILLIS = PERIOD_IN_DAYS * 24L * 60L * 60L * 1000L

    @Override
    def execute(UpdateContext context) {
        def sinceDateInMillis = currentTimeMillis() - PERIOD_IN_MILLIS
        context.databaseHelper.with {
            def startDates = sql.rows("""
                    SELECT DISTINCT startDate
                    FROM arch_process_instance
                    WHERE stateId = 0
                    AND startDate >= $sinceDateInMillis
                    ORDER BY startDate ASC""").collect { it.startDate as Long }
            def encryptedList = SimpleEncryptor.encrypt(new ObjectMapper().writeValueAsBytes(startDates))
            executeUpdate("UPDATE platform SET information = '${encryptedList}'")
        }
    }

    @Override
    String getDescription() {
        return "Initialize the platform information"
    }
}
