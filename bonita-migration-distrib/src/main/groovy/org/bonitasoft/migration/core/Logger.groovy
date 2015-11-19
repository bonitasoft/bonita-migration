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

package org.bonitasoft.migration.core

/**
 * @author Baptiste Mesta
 */
class Logger {

    def boolean debug = true
    def boolean info = true
    def boolean warn = true
    def boolean error = true


    def debug(String message) {
        if(debug)
        println "[DEBUG] " + message
    }

    def info(String message) {
        if(info)
        println "[INFO] " + message
    }

    def warn(String message) {
        if(warn)
        println "[WARN] " + message
    }

    def error(String message) {
        if(error)
        println "[ERROR] " + message
    }

    void setLevel(String level) {
        debug = true
        info = true
        warn = true
        error = true
        switch (level) {
            case "INFO":
                debug = false
                break
            case "WARN":
                debug = false
                info = false
                break
            case "ERROR":
                debug = false
                info = false
                warn = false
                break
            case "OFF":
                debug = false
                info = false
                warn = false
                error = false
                break
        }

    }
}
