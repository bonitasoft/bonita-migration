DROP INDEX "idx_datamapp_container" ON "DATA_MAPPING"
@@
DROP TABLE "DATA_MAPPING"
@@
DROP TABLE "ARCH_DATA_MAPPING"
@@
DROP INDEX idx_datai_container ON "DATA_INSTANCE"
@@
CREATE INDEX idx_datai_container ON "DATA_INSTANCE" (tenantId, containerId, containerType, name)
@@
DROP INDEX idx1_arch_data_instance ON "ARCH_DATA_INSTANCE"
@@
CREATE INDEX idx1_arch_data_instance ON "ARCH_DATA_INSTANCE" (tenantId, containerId, containerType, archiveDate, name, sourceObjectId)
@@
DELETE FROM "SEQUENCE" WHERE "ID" = 10021
@@
DELETE FROM "SEQUENCE" WHERE "ID" = 20051
@@
DELETE FROM "COMMAND" WHERE "NAME" = 'getUpdatedVariableValuesForProcessDefinition'
@@
DELETE FROM "COMMAND" WHERE "NAME" = 'getUpdatedVariableValuesForActivityInstance'
@@
DELETE FROM "COMMAND" WHERE "NAME" = 'getUpdatedVariableValuesForProcessInstance'
@@