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
package org.bonitasoft.migration.core

/**
 * @author Emmanuel Duchastenier
 */
class DisplayUtil {

    public final static String LINE_SEPARATOR = System.getProperty("line.separator");

    void printInRectangleWithTitle(String title, String... lines) {
        def list = lines.toList()
        list.add(0, title)
        def flatten = list.collect { it.split("\n") }.flatten()
        def maxSize = getMaxSize(flatten as String[])
        printLine(maxSize)
        println String.format("|%1\$-${maxSize}s|", title)
        printLine(maxSize)
        flatten.drop(1).each {
            println String.format("|%1\$-${maxSize}s|", it)
        }
        printLine(maxSize)

    }

    void printInRectangle(String... lines) {
        def maxSize = getMaxSize(lines)
        printLine(maxSize)
        lines.each {
            int spaces = maxSize - it.size()
            print "|"
            printSpaces((int) (spaces / 2))
            print it
            printSpaces(((int) (spaces / 2)) + spaces % 2)
            print "|"
            print LINE_SEPARATOR
        }
        printLine(maxSize)
    }

    private int getMaxSize(String... lines) {
        lines.collect { it.split("\n").collect { it.length() }.max() }.max() + 2
    }

    def printLine(int size) {
        print '+'
        int i = 0;
        while (i < size) {
            i++;
            print '-'
        }
        print '+'
        print LINE_SEPARATOR
    }

    def printSpaces(int size) {
        int i = 0;
        while (i < size) {
            i++;
            print ' '
        }
    }

    /**
     *
     *  Wrap the system out with ' | ' when executing the closure
     */
    static void executeWrappedWithTabs(Closure closure) {
        def stdout = System.out;
        System.setOut(new PrintStream(stdout) {
            @Override
            void println(String x) {
                stdout.print(" | ")
                stdout.println(x)
            }
        })
        closure.call()
        System.setOut(stdout);
    }
}
