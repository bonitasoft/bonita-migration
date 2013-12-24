package org.bonitasoft.migration.core;

import static junitparams.JUnitParamsRunner.$
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import junitparams.JUnitParamsRunner
import junitparams.Parameters

import org.junit.Test
import org.junit.runner.RunWith

import org.bonitasoft.migration.core.graph.TransitionGraph
import org.bonitasoft.migration.core.graph.Transition
import org.bonitasoft.migration.core.graph.Path

@RunWith(JUnitParamsRunner.class)
class MigrationRunnerTest {

    MigrationRunner runner = new MigrationRunner()

    @Parameters
    @Test
    public void check_version_in_database_and_bonita_home(String versionInDb,String  versionInBonitaHome,String  givenVersion,String returnedVersion, String message ){
        def detectedVersion = runner.checkSourceVersion(versionInDb, versionInBonitaHome, givenVersion);

        assertEquals(message, returnedVersion, detectedVersion);
    }

    private Object[] parametersForCheck_version_in_database_and_bonita_home() {
        return $(
        $("BOS-6.0", null, "6.0.2","6.0.2", "Case [6.0,6.1[: should have returned the given version"),
        $("BOS-6.0", null, "6.1.2",null,"Case [6.0,6.1[: should have returned null because the given version was > 6.1"),
        $("6.1.0", null, "6.1.1",null,"Case 6.1.0: should have returned null because the given version was != 6.1.0"),
        $("6.1.3", null, null,"6.1.3","Case 6.1.3: should have returned 6.1.3"),
        $("6.1.3", null, "6.1.3","6.1.3","Case 6.1.3: should have returned 6.1.3 (same as given version)"),
        $("6.2.0", "6.2.0", null,"6.2.0","Case >6.2.0 should have returned 6.2.0 (db=home)"),
        $("6.2.0", "6.2.0", "6.2.0","6.2.0","Case >6.2.0 should have returned 6.2.0 (db=home=given version)"),
        $("6.2.1", "6.2.1", "6.2.3",null,"Case >6.2.0 should have returned null (db=home != given version)"),
        $("6.2.1", "6.2.2", null,null,"Case >6.2.0 should have returned null (db!=home)"),
        );
    }

    @Test
    public void getMigrationPaths(){
        def File versionsFolder = mock(File.class)
        def File v602to610 = mock(File.class)
        def File v610to611 = mock(File.class)
        when(v602to610.isDirectory()).thenReturn(true)
        when(v602to610.getName()).thenReturn("6.0.2-6.1.0")
        when(v610to611.isDirectory()).thenReturn(true)
        when(v610to611.getName()).thenReturn("6.1.0-6.1.1")
        when(versionsFolder.listFiles()).thenReturn([v602to610, v610to611] as File[])
        when(versionsFolder.exists()).thenReturn(true)
        when(versionsFolder.isDirectory()).thenReturn(true)
        def versionMatrix = runner.getMigrationPaths(versionsFolder)

        assertEquals([
            new Transition(source:"6.0.2",target:"6.1.0"),
            new Transition(source:"6.1.0",target:"6.1.1")
        ],versionMatrix.transitions)
    }
}
