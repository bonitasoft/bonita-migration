package org.bonitasoft.migration.core.graph;

import static org.junit.Assert.*

import org.junit.Test


class TransitionGraphTest {

    @Test
    public void check_version_matrix_is_correct_with_list_tr_names(){
        def versions = ["6.0.2","6.1.0", "6.3.0","6.4.0"]

        def matrix = new TransitionGraph("6.0.2", versions)

        assertEquals([
            new Transition(source:"6.0.2",target:"6.1.0"),
            new Transition(source:"6.1.0",target:"6.3.0"),
            new Transition(source:"6.3.0",target:"6.4.0")
        ],matrix.transitions)
    }

    @Test
    public void check_getPaths_for_source_return_all_paths_starting_from_source(){
        def versions = [
            "6.0.2",
            "6.1.0",
            "6.4.0",
            "6.0.3",
        ]
        def paths = new TransitionGraph("6.0.2", versions).getPaths("6.0.2")
        assertEquals([
            new Path("6.0.2-6.0.3"),
            new Path("6.0.2-6.0.3-6.1.0"),
            new Path("6.0.2-6.0.3-6.1.0-6.4.0")
        ].toSet(),paths.toSet())
    }



    @Test
    public void check_getPreviousReleasedVersion_last_digit_0_1(){
        assertEquals("6.5.0",TransitionGraph.getPreviousReleasedVersion(["6.5.0"],"7.0.0"));
    }

    @Test
    public void check_getPreviousReleasedVersion_last_digit_0_2(){
        assertEquals("6.4.2",TransitionGraph.getPreviousReleasedVersion(["6.4.2"],"7.0.0"));
    }

    @Test
    public void check_getPreviousReleasedVersion_with_2_elements(){
        assertEquals("6.4.3",TransitionGraph.getPreviousReleasedVersion(["6.4.2","6.4.3"],"6.5.0"));
    }
    @Test
    public void check_getPreviousReleasedVersion_with_3_elements(){
        assertEquals("6.5.0",TransitionGraph.getPreviousReleasedVersion(["7.0.0","6.4.2","6.5.0"],"6.5.1"));
    }

    @Test(expected = IllegalStateException.class)
    public void check_getPreviousReleasedVersion_603(){
        TransitionGraph.getPreviousReleasedVersion([],"6.0.3");
    }

    @Test
    public void check_getPreviousVersionNumber_last_digit_not_0(){
        assertEquals([6,5,0],TransitionGraph.getPreviousVersionNumber([6,5,1]));
    }

    @Test
    public void check_getPreviousVersionNumber_last_digit_0(){
        assertEquals([6,4,10],TransitionGraph.getPreviousVersionNumber([6,5,0]));
    }

    @Test
    public void check_getPreviousVersionNumber_2_digit_0(){
        assertEquals([6,10,10],TransitionGraph.getPreviousVersionNumber([7,0,0]));
    }
}
