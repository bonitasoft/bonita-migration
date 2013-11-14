--
-- arch_process_instance
-- 

UPDATE arch_process_instance SET startedBy=0 WHERE startedBy IS NULL;
ALTER TABLE arch_process_instance ALTER COLUMN startedby SET NOT NULL;

ALTER TABLE arch_process_instance ADD startedbydelegate INT8;
UPDATE arch_process_instance SET startedbydelegate = startedby;
ALTER TABLE arch_process_instance ALTER COLUMN startedbydelegate SET NOT NULL;


--
-- process_instance
-- 

UPDATE process_instance SET startedBy=0 WHERE startedBy IS NULL;
ALTER TABLE process_instance ALTER COLUMN startedby SET NOT NULL;

ALTER TABLE process_instance ADD startedbydelegate INT8;
UPDATE process_instance SET startedbydelegate = startedby;
ALTER TABLE process_instance ALTER COLUMN startedbydelegate SET NOT NULL;
