package org.bonitasoft.migration.postmigration

import static org.assertj.core.api.Assertions.assertThat

class BDMPostMigrationTest extends GroovyTestCase {

    public void testCheckLatestBDMJarsArePresent() {
        def String targetVersion = PostMigrationTest.getCurrentBonitaVersion();
        def bonitaHome =  System.getProperty("bonita.home")
        def s = File.separatorChar
        def File clientJarFile = new File(bonitaHome, "server${s}platform${s}data-management${s}compilation-dependencies${s}bonita-client-" + targetVersion + ".jar");
        assertThat(clientJarFile).as("File %s does not denote an existing file", clientJarFile.getName()).exists();
        assertThat(clientJarFile).as("File %s is not a normal file", clientJarFile.getAbsolutePath()).isFile();
        println "BDM jar files have been correctly updated."
    }

    public static int runFromVersion() {
        return 700;
    }
}