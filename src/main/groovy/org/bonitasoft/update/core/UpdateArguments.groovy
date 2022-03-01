package org.bonitasoft.update.core

import groovy.transform.Immutable
import org.apache.commons.cli.*

@Immutable
class UpdateArguments {
    private static final Options OPTIONS = new Options()
            .addOption(null, "verify", false, "Only verify that the platform can be updated to the required version." +
                    " It will not update the platform")
            .addOption("h", "help", false, "Print this help");

    static UpdateArguments parse(String[] args) throws ParseException {
        DefaultParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(OPTIONS, args);
        UpdateArguments updateArguments = new UpdateArguments(
                commandLine.hasOption("help"),
                commandLine.hasOption("verify")
        );
        return updateArguments;
    }

    static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("bonita-update", OPTIONS);
    }

    boolean printHelp
    boolean verify


}
