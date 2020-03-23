package org.bonitasoft.migration.core

import groovy.transform.Immutable
import org.apache.commons.cli.*

@Immutable
class MigrationArguments {
    private static final Options OPTIONS = new Options()
            .addOption(null, "verify", false, "Only verify that the platform can be migrated to the required version." +
                    " It will not migrate the platform")
            .addOption(null, "updateCaseOverview", true, "Update Case Overviews of the selected process definition. Usage : updateCaseOverview <processDefinitionId>")
            .addOption("h", "help", false, "Print this help");

    static MigrationArguments parse(String[] args) throws ParseException {
        DefaultParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(OPTIONS, args);
        MigrationArguments migrationArguments = new MigrationArguments(
                commandLine.hasOption("help"),
                commandLine.hasOption("verify"),
                commandLine.hasOption("updateCaseOverview"),
                commandLine.getOptionValue("updateCaseOverview")
        );
        return migrationArguments;
    }

    static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("bonita-migration", OPTIONS);
    }

    boolean printHelp
    boolean verify
    boolean updateCaseOverview
    String processToUpdate


}
