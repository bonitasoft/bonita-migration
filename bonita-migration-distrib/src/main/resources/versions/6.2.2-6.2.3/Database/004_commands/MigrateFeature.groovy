
import org.bonitasoft.migration.versions.v6_3_1_to_6_3_2.CommandDescriptor
import org.bonitasoft.migration.versions.v6_3_1_to_6_3_2.UpdatedDefaultCommands

List<CommandDescriptor> commandsToInsert = []

commandsToInsert.add(
    new CommandDescriptor(
        name:"advancedStartProcessCommand", 
        implementation:"org.bonitasoft.engine.command.AdvancedStartProcessCommand", 
        description:"Advanced start process."))
new UpdatedDefaultCommands().migrate(sql, commandsToInsert)
