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
package org.bonitasoft.update.filler

import org.junit.Rule
import org.junit.rules.MethodRule
import org.junit.runners.model.Statement

/**
 * @author Baptiste Mesta
 */
class FillerRunner {

    public static void main(String[] args) {
        def classNames = args.toList()

        println "FillerRunner: fill with classes names:" + classNames


        def instances = []

        classNames.each {
            try {
                def clazz = Class.forName(it)
                instances.add(clazz.newInstance())
            } catch (ClassNotFoundException ignored) {
                println "Filler $it not found, skipping it"
            }
        }

        def statement = new Statement() {
            @Override
            void evaluate() throws Throwable {
                instances.each {
                    executeAllMethodsHaving(org.bonitasoft.update.filler.FillAction, it)
                }
            }
        }
        statement = withFillerBdmInitializer(statement, instances)
        statement = withRules(statement, instances)
        statement = withFillerInitializer(statement, instances)
        statement = withFillerShutdown(statement, instances)

        statement.evaluate()

        println "FillerRunner: finished "
        System.exit(0)
    }

    static def withFillerInitializer(Statement statement, instances) {
        return new Statement() {
            @Override
            void evaluate() throws Throwable {
                executeAllMethodsHaving(FillerInitializer, instances)
                statement.evaluate()
            }
        }
    }

    static def withFillerBdmInitializer(Statement statement, instances) {
        return new Statement() {
            @Override
            void evaluate() throws Throwable {
                executeAllMethodsHaving(FillerBdmInitializer, instances)
                statement.evaluate()
            }
        }
    }

    static def withFillerShutdown(Statement statement, instances) {
        return new Statement() {
            @Override
            void evaluate() throws Throwable {
                statement.evaluate()
                executeAllMethodsHaving(FillerShutdown, instances)
            }
        }
    }

    static def withRules(Statement statement, Object instances) {

        Statement newStatement = statement
        instances.each { instance ->
            instance.class.getDeclaredFields().each { field ->
                if (field.getAnnotation(Rule) != null) {
                    def rule = field.get(instance) as MethodRule
                    newStatement = rule.apply(newStatement, null, instance)
                }
            }
        }
        return newStatement
    }

    private static void executeAllMethodsHaving(Class clazz, def instances) {
        instances.each { instance ->
            instance.class.getMethods().each { method ->
                def annotation = method.getAnnotation(clazz)
                if (annotation != null) {
                    println("FillerRunner: Executing " + instance.class.getName() + "." + method.getName())
                    try {
                        method.invoke(instance)
                    } catch (Exception e) {
                        println "Issue while executing " + instance.class.getName() + "." + method.getName()
                        println "${e.getClass().getName()}  ${e.getMessage()}"
                        e.printStackTrace()
                        System.exit(1)
                    }
                }
            }
        }
    }
}
