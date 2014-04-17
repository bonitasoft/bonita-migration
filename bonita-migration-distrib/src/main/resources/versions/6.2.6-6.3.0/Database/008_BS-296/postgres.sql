-- change constraint name to data_mapping_containerid_key for postgres < 9.0
ALTER TABLE data_mapping DROP CONSTRAINT data_mapping_containerid_containertype_dataname_key;

ALTER TABLE data_mapping ADD CONSTRAINT data_mapping_tenantid_containerid_containertype_dataname_key UNIQUE (tenantid,containerid,containertype,dataname);
