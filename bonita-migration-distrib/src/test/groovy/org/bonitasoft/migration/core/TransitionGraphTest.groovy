package org.bonitasoft.migration.core;

import static org.junit.Assert.*

import org.junit.Test


class TransitionGraphTest {

    @Test
    public void check_version_matrix_is_correct_with_list_tr_names(){
        def trNames = ["6.0.2-6.1.0", "6.3.0-6.4.0"]

        def matrix = new TransitionGraph(trNames)

        assertEquals([
            new Transition(source:"6.0.2",target:"6.1.0"),
            new Transition(source:"6.3.0",target:"6.4.0")
        ],matrix.transitions)
    }

    @Test
    public void check_getPaths_for_source_return_all_paths_starting_from_source(){
        def transitions = [
            "6.0.2-6.1.0",
            "6.0.2-6.0.3",
            "6.1.0-6.1.1",
            "6.1.1-6.1.2",
            "6.1.0-6.2.0",
            "6.2.0-6.4.0",
            "6.1.2-6.2.0"
        ]
        def paths = new TransitionGraph(transitions).getPaths("6.0.2")
        assertEquals([
            new Path("6.0.2-6.0.3"),
            new Path("6.0.2-6.1.0"),
            new Path("6.0.2-6.1.0-6.1.1"),
            new Path("6.0.2-6.1.0-6.2.0"),
            new Path("6.0.2-6.1.0-6.1.1-6.1.2"),
            new Path("6.0.2-6.1.0-6.2.0-6.4.0"),
            new Path("6.0.2-6.1.0-6.1.1-6.1.2-6.2.0"),
            new Path("6.0.2-6.1.0-6.1.1-6.1.2-6.2.0-6.4.0")
        ].toSet(),paths.toSet())
    }

    @Test
    public void check_getPaths_return_all_paths(){
        def transitions = [
            "6.0.2-6.1.0",
            "6.0.2-6.0.3",
            "6.1.0-6.1.1",
        ]
        def paths = new TransitionGraph(transitions).getPaths()
        assertEquals([
            new Path("6.0.2-6.0.3"),
            new Path("6.0.2-6.1.0"),
            new Path("6.0.2-6.1.0-6.1.1"),
            new Path("6.0.2-6.0.3"),
            new Path("6.1.0-6.1.1")
        ].toSet(),paths.toSet())
    }

    @Test
    public void check_getStartNodes_return_start_nodes(){
        def transitions = [
            "6.0.3-6.0.4",
            "6.0.2-6.1.0",
            "6.0.2-6.0.3",
            "6.1.0-6.1.1",
            "6.1.1-6.1.2",
            "6.1.0-6.2.0",
            "6.2.0-6.4.0",
            "6.1.2-6.2.0"
        ]

        def startNodes = new TransitionGraph(transitions).getStartNodes()

        assertEquals([
            "6.0.2",
            "6.0.3",
            "6.1.0",
            "6.1.1",
            "6.1.2",
            "6.2.0"
        ],startNodes)
    }

    @Test
    public void check_getShortestPath_return_the_sortest_path(){
        def transitions = [
            "6.0.2-6.0.3",
            "6.0.2-6.1.0",
            "6.0.3-6.1.0",
            "6.1.0-6.1.1",
            "6.1.1-6.1.2",
            "6.1.0-6.2.0",
            "6.2.0-6.4.0",
            "6.1.2-6.2.0"
        ]
        def path = new TransitionGraph(transitions).getShortestPath("6.0.2","6.2.0")

        assertEquals(new Path("6.0.2-6.1.0-6.2.0"), path)
    }
}
