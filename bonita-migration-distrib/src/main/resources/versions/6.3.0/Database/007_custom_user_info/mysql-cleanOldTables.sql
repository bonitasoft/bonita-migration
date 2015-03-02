ALTER TABLE p_metadata_val DROP FOREIGN KEY fk_p_metadata_val_tenantId;
DROP INDEX fk_p_metadata_val_tenantId_idx on p_metadata_val;
DROP TABLE p_metadata_val;

ALTER TABLE p_metadata_def DROP FOREIGN KEY fk_p_metadata_def_tenantId;
DROP INDEX fk_p_metadata_def_tenantId_idx on p_metadata_def;
DROP TABLE p_metadata_def;
