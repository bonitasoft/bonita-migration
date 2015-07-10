package org.bonitasoft.migration.filler
/**
 * @author Baptiste Mesta
 */
class FillerRunner {

    public static void main(String[] args) {
        def className = args[0]

        println  "FillerRunner: fill with class name:"+className

        def fillerClass = Class.forName(className)
        def instance = fillerClass.newInstance()

        executeAllMethodsHaving(fillerClass, FillerInitializer, instance)
        executeAllMethodsHaving(fillerClass, FillAction, instance)
        executeAllMethodsHaving(fillerClass, FillerShutdown, instance)

        println  "FillerRunner: finished "


    }

    private static void executeAllMethodsHaving(Class<?> fillerClass,Class clazz, instance) {
        fillerClass.getMethods().each { method ->
            def annotation = method.getAnnotation(clazz)
            if (annotation != null) {
                println ("FillerRunner: Executing " + method.class.name + "." + annotation)
                method.invoke(instance)
            }
        }
        println ( "FillerRunner: finished " + clazz.name)
    }
}
