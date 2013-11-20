package org.bonitasoft.migration.core;

import static org.junit.Assert.*

import org.junit.Test

import org.bonitasoft.migration.core.exception.NotFoundException;

class MigrationUtilTest {
    
    def final static String FILE_SEPARATOR = System.getProperty("file.separator");

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
    public void getAndDisplayProperty(){
        // To capture output
        def ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        // Build properties
        def Properties properties = buildProperties();

        def String result = MigrationUtil.getAndDisplayProperty(properties, "os.name");
        assertEquals("Linux", result);
        // Get output
        baos.flush();
        def String standardOutput = baos.toString().replaceAll(System.getProperty("line.separator"), "");
        assertEquals("\tos.name = Linux", standardOutput);
    }
    
    @Test
    public void getAndDisplayPropertyWithWhitespaces(){
        // To capture output
        def ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        // Build properties
        def Properties properties = new Properties();
        properties.setProperty("os.name", "L\tinu x");

        def String result = MigrationUtil.getAndDisplayProperty(properties, "os.name");
        assertEquals("Linux", result);
        // Get output
        baos.flush();
        def String standardOutput = baos.toString().replaceAll(System.getProperty("line.separator"), "");
        assertEquals("\tos.name = Linux", standardOutput);
    }


    @Test(expected = NotFoundException.class)
    public void displayNotExistingProperty(){
        // Build properties
        def Properties properties = buildProperties();

        nogetAndDisplayProperty(properties, "plop");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAndDisplayPropertyWithNullProperties(){
        nogetAndDisplayProperty(null, "plop");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAndDisplayPropertyWithNullProperty(){
        // Build properties
        def Properties properties = buildProperties();

        nogetAndDisplayProperty(properties, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAndDisplayPropertyWithEmptyProperty(){
        // Build properties
        def Properties properties = buildProperties();

        nogetAndDisplayProperty(properties, "");
    }

    private void nogetAndDisplayProperty(Properties properties, String propertyName){
        // To capture output
        def ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            MigrationUtil.getAndDisplayProperty(properties, propertyName);
        } finally {
            // Get output
            baos.flush();
            def String standardOutput = baos.toString().replaceAll(System.getProperty("line.separator"), "");
            assertEquals("", standardOutput);
        }
    }

    @Test()
    public void getSqlContentWithParameter(){
        def String sqlFileContent = "UPDATE platform SET version = ':version';"
        def Map<String, String> parameters = Collections.singletonMap(":version", "6.1.0")

        def List<String> result = MigrationUtil.getSqlContent(sqlFileContent, parameters);
        assertEquals(1, result.size());
        assertEquals("UPDATE platform SET version = '6.1.0';", result.get(0));
    }

    @Test()
    public void getSqlContentWithoutParameter(){
        def String sqlFileContent = "plop"
        def Map<String, String> parameters = Collections.emptyMap();

        def List<String> result = MigrationUtil.getSqlContent(sqlFileContent, parameters);
        assertEquals(1, result.size());
        assertEquals("plop", result.get(0));
    }

    @Test()
    public void getSqlContentWithNullParameter(){
        def String sqlFileContent = "plop"

        def List<String> result = MigrationUtil.getSqlContent(sqlFileContent, null);
        assertEquals(1, result.size());
        assertEquals("plop", result.get(0));
    }


    @Test()
    public void getSqlContentToSplit(){
        def String sqlFileContent = "plop@@toto@@plip@@ "

        def List<String> result = MigrationUtil.getSqlContent(sqlFileContent, null);
        assertEquals(4, result.size());
        assertEquals("plop", result.get(0));
        assertEquals("toto", result.get(1));
        assertEquals("plip", result.get(2));
        assertEquals(" ", result.get(3));
    }

    @Test()
    public void getSqlContentWithCarriage(){
        def String sqlFileContent = "plop\rtoto\r\n"

        def List<String> result = MigrationUtil.getSqlContent(sqlFileContent, null);
        assertEquals(1, result.size());
        assertEquals("ploptoto\n", result.get(0));
    }

    @Test()
    public void getSqlFileWithSuffix(){
        def File folder = new File(FILE_SEPARATOR);
        def String dbVendor = "vendor";
        def String suffix = "plop";
        
        def File result = MigrationUtil.getSqlFile(folder, dbVendor, suffix);
        assertEquals(folder.getPath() + dbVendor + "-" + suffix + ".sql", result.getPath());
    }
    
    @Test()
    public void getSqlFileWithoutSuffix(){
        def File folder = new File(FILE_SEPARATOR);
        def String dbVendor = "vendor";
        def String suffix = "";
        
        def File result = MigrationUtil.getSqlFile(folder, dbVendor, suffix);
        assertEquals(folder.getPath() + dbVendor + ".sql", result.getPath());
    }
    
    @Test()
    public void getSqlFileWithNullSuffix(){
        def File folder = new File(FILE_SEPARATOR + "titi");
        def String dbVendor = "vendor";
        def String suffix = null;
        
        def File result = MigrationUtil.getSqlFile(folder, dbVendor, suffix);
        assertEquals(folder.getPath() + FILE_SEPARATOR + dbVendor + ".sql", result.getPath());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getSqlFileWithNullFolder(){
        def File folder = null;
        def String dbVendor = "vendor";
        def String suffix = "";
        
        MigrationUtil.getSqlFile(folder, dbVendor, suffix);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getSqlFileWithNullDBVendor(){
        def File folder = new File(FILE_SEPARATOR + "titi");
        def String dbVendor = null;
        def String suffix = "";
        
        MigrationUtil.getSqlFile(folder, dbVendor, suffix);
    }
}
