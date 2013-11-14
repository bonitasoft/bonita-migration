--
-- arch_process_instance
-- 

ALTER TABLE arch_process_instance ADD startedByDelegate NUMERIC(19,0) NOT NULL
GO


--
-- process_instance
-- 

UPDATE process_instance SET startedBy=0 WHERE startedBy IS NULL
GO
ALTER TABLE process_instance ALTER COLUMN startedBy NUMERIC(19,0) NOT NULL
GO
ALTER TABLE process_instance ADD startedByDelegate NUMERIC(19,0) NOT NULL
GO