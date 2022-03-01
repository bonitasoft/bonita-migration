/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.update.core

import org.slf4j.LoggerFactory

/**
 * @author Baptiste Mesta
 */
class Logger {

    private static final def log = LoggerFactory.getLogger(Logger.class)

    void debug(String message) {
        log.debug message
    }

    void debug(String message, Throwable t) {
        log.debug message, t
    }

    void info(String message) {
        log.info message
    }

    void warn(String message) {
        log.warn message
    }

    void error(String message) {
        log.error message
    }

    void error(String message, Throwable t) {
        log.error message, t
    }

}
