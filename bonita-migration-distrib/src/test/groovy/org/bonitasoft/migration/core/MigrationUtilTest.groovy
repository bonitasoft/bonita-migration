package org.bonitasoft.migration.core;

import static org.junit.Assert.*
import groovy.sql.Sql

import org.bonitasoft.migration.core.exception.NotFoundException
import org.dbunit.JdbcDatabaseTester
import org.junit.Test

class JobDataMapTest {

    def final static String FILE_SEPARATOR = System.getProperty("file.separator");
    final static DB_CONFIG = [
        'jdbc:h2:mem:test',
        'sa',
        ''
    ];
    final static DB_DRIVER = 'org.h2.Driver'


    @Test
    public void check_get_platform_version(){
        Sql sql
        sql = Sql.newInstance(*DB_CONFIG, DB_DRIVER);
        sql.execute('''CREATE TABLE platform (
  id BIGINT NOT NULL,
  version VARCHAR(50) NOT NULL,
  previousVersion VARCHAR(50) NOT NULL,
  initialVersion VARCHAR(50) NOT NULL,
  created BIGINT NOT NULL,
  createdBy VARCHAR(50) NOT NULL,
  PRIMARY KEY (id)
);''');
        sql.executeInsert("insert into platform values(1,'the_version','the_previous_version','the_initial_version',1,1)")
        sql.eachRow("select * from platform") { row->
            fail( "row="+row)
        }
        JdbcDatabaseTester tester = new JdbcDatabaseTester(DB_DRIVER, *DB_CONFIG)

        assertEquals("the_version",MigrationUtil.getAndDisplayPlatformVersion(sql))
        tester.onTearDown()
    }

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
        properties.setProperty("name", "Linux");
        properties.setProperty("user.function", "Developer");
        properties.setProperty("user.age", "You are too curious!");
        return properties;
    }

    @Test
    public void getAndPrintProperty(){
        // To capture output
        def ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        // Build properties
        def Properties properties = buildProperties();

        def String result = MigrationUtil.getAndPrintProperty(properties, "name");
        assertEquals("Linux", result);
        // Get output
        baos.flush();
        def String standardOutput = baos.toString().replaceAll(System.getProperty("line.separator"), "");
        assertEquals("\t-name = Linux", standardOutput);
    }


    @Test
    public void getAndPrintSystemProperty(){
        // To capture output
        def ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        // Build properties
        def Properties properties = buildProperties();

        def String result = MigrationUtil.getAndPrintProperty(properties, "os.name");
        def String propertyName = System.getProperty("os.name");
        assertEquals(propertyName, result);
        // Get output
        baos.flush();
        def String standardOutput = baos.toString().replaceAll(System.getProperty("line.separator"), "");
        assertEquals("\t-os.name = " + propertyName, standardOutput);
    }

    @Test
    public void getAndPrintPropertyWithWhitespaces(){
        // To capture output
        def ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        // Build properties
        def Properties properties = new Properties();
        properties.setProperty("name", "Linux \t");

        def String result = MigrationUtil.getAndPrintProperty(properties, "name");
        assertEquals("Linux", result);
        // Get output
        baos.flush();
        def String standardOutput = baos.toString().replaceAll(System.getProperty("line.separator"), "");
        assertEquals("\t-name = Linux", standardOutput);
    }


    @Test(expected = NotFoundException.class)
    public void getAndPrintNotExistingProperty(){
        // Build properties
        def Properties properties = buildProperties();

        noGetAndPrintProperty(properties, "plop");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAndPrintPropertyWithNullProperties(){
        noGetAndPrintProperty(null, "plop");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAndPrintPropertyWithNullProperty(){
        // Build properties
        def Properties properties = buildProperties();

        noGetAndPrintProperty(properties, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAndPrintPropertyWithEmptyProperty(){
        // Build properties
        def Properties properties = buildProperties();

        noGetAndPrintProperty(properties, "");
    }

    private void noGetAndPrintProperty(Properties properties, String propertyName){
        // To capture output
        def ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            MigrationUtil.getAndPrintProperty(properties, propertyName);
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
    public void setSystemOutWithTab(){
        // To capture output
        def ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        def PrintStream stdout = System.out;

        def PrintStream oldPrintStream = MigrationUtil.setSystemOutWithTab(3);
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

        def PrintStream oldPrintStream = MigrationUtil.setSystemOutWithTab(0);

        assertEquals(stdout, oldPrintStream);
        println "plop"
        // Get output
        baos.flush();
        def String standardOutput = baos.toString().replaceAll(System.getProperty("line.separator"), "");
        assertEquals("plop", standardOutput);

        // Clean up
        System.setOut(stdout);
    }

    @Test()
    public void printSuccessMigration(){
        // To capture output
        def ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        def Date startMigrationDate = new Date(0);
        def Date startFeatureDate = new Date(1);

        MigrationUtil.printSuccessMigration(startFeatureDate, startMigrationDate);

        // Get output
        baos.flush();
        def String standardOutput = baos.toString().replaceAll(System.getProperty("line.separator"), "");
        assertTrue(standardOutput.contains("[ Migration step success in "));
        assertTrue(standardOutput.contains(". Migration started "));
        assertTrue(standardOutput.contains(" ago. ]"));
        assertTrue(standardOutput.contains("seconds") || standardOutput.contains("days") || standardOutput.contains("hours"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void printSuccessMigrationWithNullStartFeatureDate(){
        MigrationUtil.printSuccessMigration(null, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void printSuccessMigrationWithNullStartMigrationDate(){
        MigrationUtil.printSuccessMigration(new Date(), null);
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
