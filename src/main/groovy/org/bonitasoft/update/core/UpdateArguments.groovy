/**
 * Copyright (C) 2024 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.update.core

import groovy.transform.Immutable
import org.apache.commons.cli.*

@Immutable
class UpdateArguments {
    private static final Options OPTIONS = new Options()
    .addOption(null, "verify", false, "Only verify that the platform can be updated to the required version." +
    " It will not update the platform")
    .addOption("h", "help", false, "Print this help")

    static UpdateArguments parse(String[] args) throws ParseException {
        DefaultParser parser = new DefaultParser()
        CommandLine commandLine = parser.parse(OPTIONS, args)
        UpdateArguments updateArguments = new UpdateArguments(
                commandLine.hasOption("help"),
                commandLine.hasOption("verify")
                )
        return updateArguments
    }

    static void printHelp() {
        HelpFormatter formatter = new HelpFormatter()
        formatter.printHelp("bonita-update", OPTIONS)
    }

    boolean printHelp
    boolean verify
}
