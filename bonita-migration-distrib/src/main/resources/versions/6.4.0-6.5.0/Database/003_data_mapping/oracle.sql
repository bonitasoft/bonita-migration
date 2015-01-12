DROP TABLE "DATA_MAPPING"@@
DROP TABLE "ARCH_DATA_MAPPING"@@
CREATE INDEX "IDX_DATAI_CONTAINER" ON "DATA_INSTANCE" (tenantId, containerId, containerType, name)@@
DROP INDEX "IDX1_ARCH_DATA_INSTANCE"@@
CREATE INDEX "IDX1_ARCH_DATA_INSTANCE" ON "ARCH_DATA_INSTANCE" (tenantId, containerId, containerType, archiveDate, name, sourceObjectId)@@
DELETE FROM "SEQUENCE" WHERE "ID" = 10021 @@
DELETE FROM "SEQUENCE" WHERE "ID" = 20051 @@
DELETE FROM "COMMAND" WHERE "NAME" = 'getUpdatedVariableValuesForProcessDefinition'@@
DELETE FROM "COMMAND" WHERE "NAME" = 'getUpdatedVariableValuesForActivityInstance'@@
DELETE FROM "COMMAND" WHERE "NAME" = 'getUpdatedVariableValuesForProcessInstance'@@