/**
 * Copyright (C) 2017 BonitaSoft S.A.
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

/**
 * @author Emmanuel Duchastenier
 */
class DisplayUtil {

    Logger logger

    void logWarningsInRectangleWithTitle(String title, String... lines) {
        def list = lines.toList()
        list.add(0, title)
        def flatten = list.collect { it.split("\n") }.flatten()
        def maxSize = getMaxSize(flatten as String[])
        logger.warn dashLine(maxSize)
        logger.warn String.format("|%1\$-${maxSize}s|", title)
        logger.warn dashLine(maxSize)
        flatten.drop(1).each {
            logger.warn String.format("|%1\$-${maxSize}s|", it)
        }
        logger.warn dashLine(maxSize)
    }

    void logInfoCenteredInRectangle(String... lines) {
        def maxSize = getMaxSize(lines)
        logger.info dashLine(maxSize)
        lines.each { logger.info centeredLineInRectangle(it, maxSize) }
        logger.info dashLine(maxSize)
    }

    private int getMaxSize(String... lines) {
        lines.collect { it.split("\n").collect { it.length() }.max() }.max() + 2
    }

    String centeredLineInRectangle(String line, int innerSize) {
        int totalSpaces = innerSize - line.size()
        int spacesBefore = (int) (totalSpaces / 2)
        int spacesAfter = totalSpaces - spacesBefore
        '|' + spaces(spacesBefore) + line + spaces(spacesAfter) + '|'
    }

    String dashLine(int size) {
        if (size <= 0 ) {
            return '++'
        }
        '+' + (1..size).collect{'-'}.join('') + '+'
    }

    String spaces(int size) {
        if (size <= 0 ) {
            return ''
        }
        (1..size).collect{' '}.join('')
    }

}
