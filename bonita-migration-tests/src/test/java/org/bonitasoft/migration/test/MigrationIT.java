package org.bonitasoft.migration.test;

import java.lang.reflect.Method;

import org.junit.Test;

public class MigrationIT {

    @Test
    public void checkResult() throws Exception {
        String targetVersion = System.getProperty("target.version");
        System.out.println("Test: target = " + targetVersion);
        Class<?> checker = Class.forName("org.bonitasoft.migration.DatabaseChecker" + targetVersion.replace('.', '_'));
        Method method = checker.getMethod("verify");
        method.invoke(null);
        // if()
    }

}
