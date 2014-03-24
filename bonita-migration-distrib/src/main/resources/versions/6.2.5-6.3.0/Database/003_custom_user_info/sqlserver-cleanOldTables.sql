ALTER TABLE p_metadata_def DROP CONSTRAINT fk_p_metadata_def_tenantId
GO
DROP TABLE p_metadata_val
GO

ALTER TABLE p_metadata_val DROP CONSTRAINT fk_p_metadata_val_tenantId
GO
DROP TABLE p_metadata_def
GO