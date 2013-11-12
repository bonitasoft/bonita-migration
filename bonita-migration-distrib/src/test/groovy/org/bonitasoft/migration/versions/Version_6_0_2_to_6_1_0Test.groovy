package org.bonitasoft.migration.versions;

import static org.junit.Assert.*
import static org.mockito.Matchers.*
import static org.mockito.Mockito.*
import groovy.sql.Sql

import org.junit.Ignore
import org.junit.Test


class Version_6_0_2_to_6_1_0Test {

    @Ignore("not finished")
    @Test
    public void testExecuteSqlFiles() throws Exception {
        def sql = mock(Sql.class);
        def version = new Version_6_0_2_to_6_1_0()
        def resources = new File("resources")
        resources.mkdir()
        //        def createSubFolder= {parent,name->new File()}
        //        resources.
        //
        //        version.executeSqlFiles(null, "", null)
    }
}
