/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.migration;

import java.io.InputStream;
import java.util.Map;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class DatabaseFiller6_2_3 extends SimpleDatabaseFiller6_0_2 {
    
    public static void main(final String[] args) throws Exception {
        DatabaseFiller6_2_3 databaseFiller = new DatabaseFiller6_2_3();
        databaseFiller.execute(1, 1, 1, 1);
    }

    protected InputStream getProfilesXMLStream() {
        return getClass().getResourceAsStream("profiles.xml");
    }


    @Override
    public void shutdown() throws Exception {
        super.shutdown();
        for (Map.Entry<Thread, StackTraceElement[]> threadEntry : Thread.getAllStackTraces().entrySet()) {
            System.out.println(threadEntry.getKey()+": " +threadEntry.getValue()[0]);
        }
    }
}
