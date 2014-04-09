ALTER TABLE p_metadata_val DROP CONSTRAINT fk_p_metadata_val_tenantId
 @@
DROP TABLE p_metadata_val
 @@

ALTER TABLE p_metadata_def DROP CONSTRAINT fk_p_metadata_def_tenantId
 @@
DROP TABLE p_metadata_def
 @@
