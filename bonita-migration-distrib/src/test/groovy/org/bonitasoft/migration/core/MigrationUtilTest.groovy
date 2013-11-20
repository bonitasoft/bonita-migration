package org.bonitasoft.migration.core;

import static org.junit.Assert.*

import org.gmock.GMockTestCase
import org.junit.Test

import org.bonitasoft.migration.core.exception.NotFoundException;

class MigrationUtilTest {

    @Test
    public void getProperties(){
        // Init properties
        def Properties properties = buildProperties();
        saveProperties(properties, "Config.properties");

        def Properties result =  MigrationUtil.getProperties();
        assertEquals(properties, result);

        // Clean up
        def File file = new File("Config.properties");
        boolean success = file.delete();
        if (!success)
            throw new IllegalArgumentException("Delete: deletion failed");
    }

    @Test(expected = NotFoundException.class)
    public void notGetProperties(){
        MigrationUtil.getProperties();
    }

    private void saveProperties(Properties props, String fileLocation) throws FileNotFoundException, IOException {
        def OutputStream out = new FileOutputStream(fileLocation);
        props.store(out, "");
        out.flush();
        out.close();
    }

    private Properties buildProperties(){
        def Properties properties = new Properties();
        properties.setProperty("user.name", "HackTrack");
        properties.setProperty("os.name", "Linux");
        properties.setProperty("user.function", "Developer");
        properties.setProperty("user.age", "You are too curious!");
        return properties;
    }

    @Test
    public void displayProperty(){
        // To capture output
        def ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        // Build properties
        def Properties properties = buildProperties();

        def String result = MigrationUtil.displayProperty(properties, "os.name");
        assertEquals("Linux", result);
        // Get output
        baos.flush();
        def String standardOutput = baos.toString().replaceAll(System.getProperty("line.separator"), "");
        assertEquals("os.name = Linux", standardOutput);
    }


    @Test(expected = NotFoundException.class)
    public void displayNotExistingProperty(){
        // Build properties
        def Properties properties = buildProperties();
        
        noDisplayProperty(properties, "plop");
    }

    @Test(expected = IllegalArgumentException.class)
    public void displayPropertyWithNullProperties(){
        noDisplayProperty(null, "plop");
    }

    @Test(expected = IllegalArgumentException.class)
    public void displayPropertyWithNullProperty(){
        // Build properties
        def Properties properties = buildProperties();
        
        noDisplayProperty(properties, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void displayPropertyWithEmptyProperty(){
        // Build properties
        def Properties properties = buildProperties();
        
        noDisplayProperty(properties, "");
    }

    private void noDisplayProperty(Properties properties, String propertyName){
        // To capture output
        def ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        def String result;
        try {
            result = MigrationUtil.displayProperty(properties, propertyName);
        } finally {
            // Get output
            baos.flush();
            def String standardOutput = baos.toString().replaceAll(System.getProperty("line.separator"), "");
            assertEquals("", standardOutput);
        }
    }




    //	@Test
    //	public void testExecuteDefaultSqlFile(){
    //		play {
    //			mockSql.load("withTransaction").returns("apple")
    //			MigrationUtil.executeDefaultSqlFile( file, dbVendor, sql)
    //		}
    //	}
    //
    //
    //	@Test
    //	public void testExecuteDefaultSqlNotExistingFile(){
    //		play {
    //			MigrationUtil.executeDefaultSqlFile( file, dbVendor,  sql)
    //		}
    //	}
    //
    //	@Test
    //	public void testExecuteDefaultSqlNoFile(){
    //		play {
    //			MigrationUtil.executeDefaultSqlFile(null, dbVendor, sql)
    //		}
    //	}
    //
    //
    //	@Test
    //	public void testExecuteDefaultSqlFileNotExistingVendor(){
    //		play {
    //			MigrationUtil.executeDefaultSqlFile(file, "plop", sql)
    //		}
    //	}
    //
    //	@Test
    //	public void testExecuteDefaultSqlFileNoVendor(){
    //		play {
    //			MigrationUtil.executeDefaultSqlFile(file, null, sql)
    //		}
    //	}
    //
    //	@Test
    //	public void testExecuteDefaultSqlFileNoSql(){
    //		play {
    //			MigrationUtil.executeDefaultSqlFile(file, dbVendor, null)
    //		}
    //	}
}
