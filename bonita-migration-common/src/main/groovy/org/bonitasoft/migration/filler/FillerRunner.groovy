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

import org.junit.Rule
import org.junit.rules.MethodRule
import org.junit.runners.model.Statement

/**
 * @author Baptiste Mesta
 */
class FillerRunner {

    public static void main(String[] args) {
        def className = args[0]

        println "FillerRunner: fill with class name:" + className

        def fillerClass = Class.forName(className)
        def instance = fillerClass.newInstance()

        def statement = new Statement(){
            @Override
            void evaluate() throws Throwable {
                executeAllMethodsHaving(fillerClass, FillAction, instance)
            }
        }
        statement = withFillerBdmInitializer(statement, fillerClass, instance)
        statement = withRules(statement, fillerClass, instance);
        statement = withFillerInitializer(statement, fillerClass, instance)
        statement = withFillerShutdown(statement, fillerClass, instance)

        statement.evaluate()

        println "FillerRunner: finished "
        System.exit(0)

    }
    static def withFillerInitializer(Statement statement, fillerClass, instance){
        return new Statement(){
            @Override
            void evaluate() throws Throwable {
                executeAllMethodsHaving(fillerClass, FillerInitializer, instance)
                statement.evaluate()
            }
        }
    }
    static def withFillerBdmInitializer(Statement statement, fillerClass, instance){
        return new Statement(){
            @Override
            void evaluate() throws Throwable {
                executeAllMethodsHaving(fillerClass, FillerBdmInitializer, instance)
                statement.evaluate()
            }
        }
    }
    static def withFillerShutdown(Statement statement, fillerClass, instance){
        return new Statement(){
            @Override
            void evaluate() throws Throwable {
                statement.evaluate()
                executeAllMethodsHaving(fillerClass, FillerShutdown, instance)
            }
        }
    }

    static def withRules(Statement statement, Class<?> aClass, Object instance) {
        Statement newStatement = statement
        aClass.getDeclaredFields().each { field -> if(field.getAnnotation(Rule) != null){
           def rule = field.get(instance) as MethodRule
            newStatement = rule.apply(newStatement,null,instance)
        }};
        return newStatement
    }

    private static void executeAllMethodsHaving(Class<?> fillerClass, Class clazz, instance) {
        fillerClass.getMethods().each { method ->
            def annotation = method.getAnnotation(clazz)
            if (annotation != null) {
                println("FillerRunner: Executing " + fillerClass.getName() + "." + method.getName())
                try {
                    method.invoke(instance)
                } catch (Exception e) {
                    println "Issue while executing " + fillerClass.getName() + "." + method.getName()
                    println "${e.getClass().getName()}  ${e.getMessage()}"
                    e.printStackTrace()
                    System.exit(1)
                }
            }
        }
    }
}
