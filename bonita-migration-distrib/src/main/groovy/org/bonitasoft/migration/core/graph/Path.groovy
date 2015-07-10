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




/**
 *
 * Launched by Migration.groovy
 *
 * @author Baptiste Mesta
 *
 */
@EqualsAndHashCode
public class Path {

    List<Transition> transitions;

    Path(Transition transition){
        transitions = [transition]
    }
    Path(Path original, Transition transition){
        transitions = []
        transitions.addAll(original.transitions)
        transitions.add(transition)
    }

    void add(Transition transition){
        transitions.add(transition)
    }

    Path(String pathAsString){
        def split = pathAsString.split("-")
        def transitions = []
        for(int i=0; i<split.length-1;i++){
            transitions.add(new Transition(source:split[i],target:split[i+1]))
        }
        this.transitions = transitions;
    }

    @Override
    public String toString() {
        def str = transitions.get(0).source
        transitions.each { str += " -> ${it.target}" }
        return str;
    }

    String getLastVersion(){
        return transitions.last().target
    }

    int getSize(){
        transitions.size()
    }
}
