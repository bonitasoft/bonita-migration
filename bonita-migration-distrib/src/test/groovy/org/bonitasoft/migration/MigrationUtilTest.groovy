package org.bonitasoft.migration;

import static org.junit.Assert.*

import org.junit.Test


class MigrationUtilTest {

    @Test
    public void testParseOrAskArgs() throws Exception {

        def resultMap = new MigrationUtil().parseOrAskArgs("--key1","value1","--key2","value2")

        assertTrue(["key1":"value1","key2":"value2"].equals(resultMap));
    }
}
