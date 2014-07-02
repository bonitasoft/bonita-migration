
import org.bonitasoft.migration.versions.v6_3_1_to_6_3_2.CommandDescriptor
import org.bonitasoft.migration.versions.v6_3_1_to_6_3_2.UpdatedDefaultCommands

List<CommandDescriptor> commandsToInsert = []

commandsToInsert.add(
    new CommandDescriptor(
        name:"canStartProcessDefinition", 
        implementation:"org.bonitasoft.engine.external.permission.CanStartProcessDefinition", 
        description:"Return true if the user can start the process, false otherwise. Use parameter key USER_ID_KEY and PROCESS_DEFINITION_ID_KEY"))
commandsToInsert.add(
    new CommandDescriptor(
        name:"isInvolvedInHumanTask", 
        implementation:"org.bonitasoft.engine.external.permission.IsInvolvedInHumanTask", 
        description:"Return true if a user is involved in a specific human task, false otherwise. Use parameter key USER_ID_KEY and HUMAN_TASK_INSTANCE_ID_KEY"))
commandsToInsert.add(
    new CommandDescriptor(
        name:"advancedStartProcessCommand",
        implementation:"org.bonitasoft.engine.command.AdvancedStartProcessCommand",
        description:"Advanced start process."))
new UpdatedDefaultCommands().migrate(sql, commandsToInsert, ["getActorIdsForUserIdIncludingTeam"])
