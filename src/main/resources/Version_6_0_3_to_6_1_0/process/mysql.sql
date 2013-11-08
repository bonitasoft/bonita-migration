UPDATE arch_process_instance SET startedBy = 0 WHERE startedBy IS NULL;
ALTER TABLE arch_process_instance MODIFY COLUMN startedBy BIGINT(20) NOT NULL;
ALTER TABLE arch_process_instance ADD startedByDelegate BIGINT(20) NOT NULL;
ALTER TABLE process_instance MODIFY COLUMN startedBy BIGINT(20) NOT NULL;
ALTER TABLE process_instance ADD startedByDelegate BIGINT(20) NOT NULL;