package org.bonitasoft.migration.filler

import java.lang.reflect.Method

/**
 * @author Baptiste Mesta
 */
class FillerRunner {

    public static void main(String[] args){
        def className = args[0]

        def fillerClass = Class.forName(className)


        def instance = fillerClass.newInstance()

        executeAllMethodsHaving(fillerClass, FillerInitializer, instance)
        executeAllMethodsHaving(fillerClass, FillAction, instance)
        executeAllMethodsHaving(fillerClass, FillerShutdown, instance)

    }

    private static Method[] executeAllMethodsHaving(Class<?> fillerClass, clazz, instance) {
        return fillerClass.getMethods().each { method ->


            def annotation = method.getAnnotation(clazz)
            if (annotation != null) {
                method.invoke(instance)
            }
        }
    }
}
