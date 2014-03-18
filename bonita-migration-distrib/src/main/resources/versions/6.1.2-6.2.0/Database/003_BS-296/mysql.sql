ALTER TABLE data_mapping DROP INDEX containerId;
ALTER TABLE data_mapping ADD CONSTRAINT tenantid UNIQUE (tenantid,containerId,containerType,dataName);