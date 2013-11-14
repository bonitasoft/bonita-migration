package org.bonitasoft.migration;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DatabaseFillerTest {

    private static DatabaseFiller databaseFiller;

    @BeforeClass
    public static void beforeClass() throws Exception {
        databaseFiller = new DatabaseFiller();
        databaseFiller.setup();
        System.out.println("Finished init");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        databaseFiller.shutdown();
    }

    @Test
    public void testExecuteWithOneElement() throws Exception {
        Map<String, String> stats = databaseFiller.fillDatabase(1, 1, 1);
        assertEquals("1", stats.get("Process definitions"));
        assertEquals("1", stats.get("Process instances"));
        assertEquals("1", stats.get("Waiting events"));
    }
}
