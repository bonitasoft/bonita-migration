/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.migration.core.graph

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


/**
 *
 * Launched by Migration.groovy
 *
 * @author Baptiste Mesta
 *
 */
@EqualsAndHashCode
@ToString
public class TransitionGraph {

    List<Transition> transitions = [];

    TransitionGraph(String firstVersion, List<String> versions) {
        versions.each { target ->
            if(!target.equals(firstVersion)){
                transitions.add(new Transition(source: getPreviousReleasedVersion(versions, target), target: target))
            }
        }
    }

    static String getPreviousReleasedVersion(List<String> names, String target) {
        def List<Integer> split = target.split("\\.").collect { Integer.valueOf(it) }
        split = getPreviousVersionNumber(split)
        while (!names.contains(versionAsString(split))) {
            if (split[0] <= 5) {
                throw new IllegalStateException("no previous version for $target found");
            }
            split = getPreviousVersionNumber(split)
        }

        return versionAsString(split)
    }


    static String versionAsString(List<Integer> split) {
        return split[0] + "." + split[1] + "." + split[2]
    }

    static List<Integer> getPreviousVersionNumber(List<Integer> split) {
        if (split[2].equals(0)) {
            if (split[1].equals(0)) {
                split[0] -= 1
                split[1] = 10
                split[2] = 10
            } else {
                split[1] -= 1
                split[2] = 10
            }
        } else {
            split[2] -= 1
        }
        return split
    }

    List<Path> getPaths() {
        def pathMap = transitions.groupBy { it.source }
        def List<Path> paths = []
        pathMap.each {
            paths.addAll(getPaths(it.key))
        }
        return paths;
    }


    List<Path> getPaths(String source) {
        Map transitionStartingWithMap = transitions.groupBy { it.source }
        return getPaths(source, null, transitionStartingWithMap);
    }

    List<Path> getPaths(String startNode, Path sourcePath, Map transitionStartingWithMap) {
        List<Transition> followingTransitions = transitionStartingWithMap.get(startNode)
        List<Path> paths = []
        followingTransitions.each {
            def newPath = (sourcePath == null ? new Path(it) : new Path(sourcePath, it))
            paths.add(newPath);
            paths.addAll(getPaths(it.target, newPath, transitionStartingWithMap))
        }
        return paths;
    }

    List<String> getStartNodes() {
        return transitions.collect { it.source }.unique().sort();
    }

    Path getShortestPath(String source, String target) {
        getPaths(source).findAll {
            it.getLastVersion() == target
        }.sort(false, { a, b -> a.getSize() <=> b.getSize() })[0]
    }
}
