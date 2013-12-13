package org.bonitasoft.migration.core;

import static org.junit.Assert.*

import org.bonitasoft.migration.core.exception.NotFoundException
import org.junit.Test

class IOUtilTest {

    def final static String FILE_SEPARATOR = System.getProperty("file.separator");

    @Test()
    public void setSystemOutWithTab(){
        // To capture output
        def ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        def PrintStream stdout = System.out;

        def PrintStream oldPrintStream = IOUtil.setSystemOutWithTab(3);
        assertEquals(stdout, oldPrintStream);
        println "plop"
        // Get output
        baos.flush();
        def String standardOutput = baos.toString().replaceAll(System.getProperty("line.separator"), "");
        assertEquals("   |   |   | plop", standardOutput);

        // Clean up
        System.setOut(stdout);
    }

    @Test()
    public void setSystemOutWithoutTab(){
        // To capture output
        def ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        def PrintStream stdout = System.out;

        def PrintStream oldPrintStream = IOUtil.setSystemOutWithTab(0);

        assertEquals(stdout, oldPrintStream);
        println "plop"
        // Get output
        baos.flush();
        def String standardOutput = baos.toString().replaceAll(System.getProperty("line.separator"), "");
        assertEquals("plop", standardOutput);

        // Clean up
        System.setOut(stdout);
    }

}
