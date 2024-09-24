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
package org.bonitasoft.update

import org.bonitasoft.update.core.Logger

class TestLogger extends Logger {

    List<String> debugLogs = []
    List<String> infoLogs = []
    List<String> warnLogs = []
    List<String> errorLogs = []

    @Override
    void debug(String message) {
        debugLogs.add(message)
        super.debug(message)
    }

    @Override
    void info(String message) {
        infoLogs.add(message)
        super.info(message)
    }

    @Override
    void warn(String message) {
        warnLogs.add(message)
        super.warn(message)
    }

    @Override
    void error(String message) {
        errorLogs.add(message)
        super.error(message)
    }

    void clear(){
        debugLogs.clear()
        infoLogs.clear()
        warnLogs.clear()
        errorLogs.clear()
    }
}
