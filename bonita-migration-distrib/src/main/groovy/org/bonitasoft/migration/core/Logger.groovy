package org.bonitasoft.migration.core

/**
 * @author Baptiste Mesta
 */
class Logger {

    def debug(String message) {
        println "[DEBUG] " + message
    }

    def info(String message) {
        println "[INFO] " + message
    }

    def warn(String message) {
        println "[WARN] " + message
    }

    def error(String message) {
        println "[ERROR] " + message
    }

}
