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

package org.bonitasoft.migration.core

/**
 * @author Elias Ricken de Medeiros
 */
class Reporter {

    private List<String> warnings = []

    void addWarning(String warning) {
        warnings.add(warning)
    }

    List<String> getWarnings() {
        return Collections.unmodifiableList(warnings)
    }

    String getWarningHeader(){
        return warnings.isEmpty()? "" : """
***************************************************************************************
/!\\  The platform was successfully migrated. However, some points need your attention.
***************************************************************************************
"""
    }

    void printReport(){
        println warningHeader
        warnings.each {warning ->
            println warning
        }

    }

}
