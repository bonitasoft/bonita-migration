import org.bonitasoft.migration.versions.v6_3_1_to_6_3_2.CommandDescriptor
import org.bonitasoft.migration.versions.v6_3_1_to_6_3_2.UpdatedDefaultCommands

/*
 move to 7.0.0 when available
  */
CommandDescriptor getBusinessDataByIdCommand = new CommandDescriptor(
        name: "getBusinessDataById",
        implementation: "org.bonitasoft.engine.command.GetBusinessDataByIdCommand",
        description: "Get the business data via its identifier and class name, and returns its Json representation.")

CommandDescriptor executeBDMQueryCommand = new CommandDescriptor(
        name: "executeBDMQuery",
        implementation: "org.bonitasoft.engine.command.ExecuteBDMQueryCommand",
        description: "Execute a named query in the BDM. Use parameter keys : \"queryName\" the name of the query in the bdm, \"returnType\" the query expected return type, \"returnsList\" if result is a List or a single value, \"queryParameters\" a Map to value query parameters")

CommandDescriptor getBusinessDataByQueryCommandDescriptor = new CommandDescriptor(
        name: "getBusinessDataByQueryCommand",
        implementation: "org.bonitasoft.engine.command.GetBusinessDataByQueryCommand",
        description: "Execute a named query in the BDM, and returns its Json representation. Use parameter keys : \"queryName\" the name of the query in the bdm, \"returnType\" the query expected return type, \"returnsList\" if result is a List or a single value, \"queryParameters\" a Map to value query parameters")


new UpdatedDefaultCommands().migrate(sql, [getBusinessDataByIdCommand,executeBDMQueryCommand,getBusinessDataByQueryCommandDescriptor], [])