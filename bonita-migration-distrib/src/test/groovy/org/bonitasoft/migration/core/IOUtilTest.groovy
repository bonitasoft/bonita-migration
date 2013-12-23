package org.bonitasoft.migration.core;

import static org.junit.Assert.*

import org.bonitasoft.migration.core.exception.NotFoundException
import org.junit.Test

class IOUtilTest {

    def final static String FILE_SEPARATOR = System.getProperty("file.separator");

    @Test()
    public void executeWrappedWithTabs(){
        def PrintStream stdout = System.out;
        
        // To capture output
        def ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        IOUtil.executeWrappedWithTabs {
            IOUtil.executeWrappedWithTabs {
                IOUtil.executeWrappedWithTabs { println "plop" }
            }
        }
        // Get output
        baos.flush();
        def String standardOutput = baos.toString().replaceAll(System.getProperty("line.separator"), "");
        assertEquals(" |  |  | plop", standardOutput);

        // Clean up
        System.setOut(stdout);
    }

    @Test()
    public void executeWrappedWithOneTab(){
        def PrintStream stdout = System.out;
        
        // To capture output
        def ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
       
        IOUtil.executeWrappedWithTabs { println "plop" }
        // Get output
        baos.flush();
        def String standardOutput = baos.toString().replaceAll(System.getProperty("line.separator"), "");
        assertEquals(" | plop", standardOutput);

        // Clean up
        System.setOut(stdout);
    }

}
