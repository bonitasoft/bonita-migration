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

    TransitionGraph(List<String> transitionNames){
        transitions = transitionNames.findAll { it.contains("-") }.collect {
            def split = it.split("-")
            new Transition(source:split[0], target:split[1])
        }
    }

    List<Path> getPaths(){
        def pathMap = transitions.groupBy{ it.source }
        def List<Path> paths = []
        pathMap.each {
            paths.addAll(getPaths(it.key))
        }
        return paths;
    }


    List<Path> getPaths(String source){
        Map transitionStartingWithMap = transitions.groupBy{ it.source }
        return getPaths(source,null,transitionStartingWithMap);
    }

    List<Path> getPaths(String startNode, Path sourcePath, Map transitionStartingWithMap){
        List<Transition> followingTransitions = transitionStartingWithMap.get(startNode)
        List<Path> paths =[]
        followingTransitions.each {
            def newPath = (sourcePath == null ? new Path(it):new Path(sourcePath,it))
            paths.add(newPath);
            paths.addAll(getPaths(it.target, newPath, transitionStartingWithMap))
        }
        return paths;
    }

    List<String> getStartNodes(){
        return transitions.collect{ it.source }.unique().sort();
    }

    Path getShortestPath(String source, String target){
        getPaths(source).findAll{ it.getLastVersion() == target }.sort(false,{a,b -> a.getSize() <=> b.getSize()})[0]
    }
}
