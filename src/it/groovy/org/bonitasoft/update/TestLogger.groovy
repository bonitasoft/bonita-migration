package org.bonitasoft.update

import org.bonitasoft.update.core.Logger

class TestLogger extends Logger {

    List<String> debugLogs = []
    List<String> infoLogs = []
    List<String> warnLogs = []
    List<String> errorLogs = []

    @Override
    void debug(String message) {
        debugLogs.add(message)
        super.debug(message)
    }

    @Override
    void info(String message) {
        infoLogs.add(message)
        super.info(message)
    }

    @Override
    void warn(String message) {
        warnLogs.add(message)
        super.warn(message)
    }

    @Override
    void error(String message) {
        errorLogs.add(message)
        super.error(message)
    }

    void clear(){
        debugLogs.clear()
        infoLogs.clear()
        warnLogs.clear()
        errorLogs.clear()
    }


}
