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

package org.bonitasoft.migration.filler
/**
 * @author Baptiste Mesta
 */
class FillerRunner {

    public static void main(String[] args) {
        def className = args[0]

        println "FillerRunner: fill with class name:" + className

        def fillerClass = Class.forName(className)
        def instance = fillerClass.newInstance()

        executeAllMethodsHaving(fillerClass, FillerInitializer, instance)
        executeAllMethodsHaving(fillerClass, FillAction, instance)
        executeAllMethodsHaving(fillerClass, FillerShutdown, instance)

        println "FillerRunner: finished "


    }

    private static void executeAllMethodsHaving(Class<?> fillerClass, Class clazz, instance) {
        fillerClass.getMethods().each { method ->
            def annotation = method.getAnnotation(clazz)
            if (annotation != null) {
                println("FillerRunner: Executing " + fillerClass.getName() + "." + method.getName())
                method.invoke(instance)
            }
        }
    }
}
