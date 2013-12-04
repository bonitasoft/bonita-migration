--
-- arch_process_instance
-- 

ALTER TABLE arch_process_instance ADD startedByDelegate NUMERIC(19,0) NOT NULL DEFAULT startedBy
@@


--
-- process_instance
-- 

ALTER TABLE process_instance ALTER COLUMN startedBy NUMERIC(19,0) NOT NULL DEFAULT 0
@@
ALTER TABLE process_instance ADD startedByDelegate NUMERIC(19,0) NOT NULL DEFAULT startedBy
@@