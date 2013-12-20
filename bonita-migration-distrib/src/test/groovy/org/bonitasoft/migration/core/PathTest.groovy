package org.bonitasoft.migration.core;

import static org.junit.Assert.*

import org.junit.Test


class PathTest {


    @Test
    void check_path_from_name() {
        def pathAsString = "6.0.2-6.1.0-6.1.1"

        def path = new Path(pathAsString)

        assertEquals([
            new Transition(source:"6.0.2",target:"6.1.0"),
            new Transition(source:"6.1.0",target:"6.1.1")
        ], path.transitions)
    }
}
