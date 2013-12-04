package org.bonitasoft.migration;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

@RunWith(AllTests.class)
public class MigrationIT {

    public static TestSuite suite() throws Exception
    {
        TestSuite suite = new TestSuite();
        String nameOfClassToRun = "org.bonitasoft.migration.DatabaseChecker" + System.getProperty("target.version").replace('.', '_');
        System.out.println("test class:" + nameOfClassToRun);
        Class<?> testClass = Class.forName(nameOfClassToRun);
        System.out.println(testClass);
        suite.addTest(new JUnit4TestAdapter(testClass));
        suite.addTest(new JUnit4TestAdapter(TestFailure.class));
        return suite;
    }
}
