Live migration tool
=======================

**Live migration tool** can be run once the migration in completed to reintegrate data from `arch_contract_data_backup` table.

# Pre-requisites

This tool can be run on a bonita runtime in version greater than or equal to 7.7.0.
The runtime can be shutdown or running.

# Configuration

Enter database configuration properties in the file `application.properties`

If you are using mysql, this migration can be very long. In that case consider changing the parameter:
`org.bonitasoft.engine.migration.delete_after_migration` (see comments in `application.properties` file) 


# Run the live migration tool

example (Unix):
>    ./bin/live-migration

example (Windows):
>    bin/live-migration.bat
