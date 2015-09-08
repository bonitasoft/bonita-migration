package org.bonitasoft.migration.version.to7_1_0

import org.bonitasoft.migration.core.MigrationContext
import org.junit.Test
/**
 * @author Laurent Leseigneur
 */
class MigratePlatformTest extends GroovyTestCase {
    @Test
    void migratePlatform(){
        def platform = new MigratePlatform()
        def migrationContext=new MigrationContext()
        def execute = platform.execute(migrationContext)
        execute
    }
}
