package org.bonitasoft.migration;

import static org.junit.Assert.*

import org.junit.Test


class MigrationTest {

    //    @Test
    //    public void testExecute() throws Exception {
    //        new Migration().execute();
    //    }
    //
    //    @Test
    //    public void testConfirmProperties() throws Exception {
    //        new Migration().confirmProperties();
    //    }
    //
    //    @Test
    //    public void testLoadLibs() throws Exception {
    //        new Migration().loadLibs();
    //    }

    @Test
    public void testFilterChildrenHavingExtension() throws Exception {
        def folder = new File("folder")
        folder.mkdir();
        def bGroovy = new File(folder,"b.groovy")
        bGroovy.createNewFile();
        def bSql = new File(folder,"b.sql")
        bSql.createNewFile();
        def aGroovy = new File(folder,"a.groovy")
        aGroovy.createNewFile();
        def aSql = new File(folder,"a.sql")
        aSql.createNewFile();

        def expected = [aSql, bSql];
        def result = new Migration().filterChildrenHavingExtension(folder, ".sql");
        bSql.delete();aSql.delete();bGroovy.delete();aGroovy.delete();folder.delete()


        assertTrue("expected=<"+expected+"> but was <"+result+">",expected.equals(result));

    }
}
