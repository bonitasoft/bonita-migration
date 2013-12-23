--
-- qrtz_fired_triggers
-- 

ALTER TABLE qrtz_fired_triggers ADD sched_time INT8 NOT NULL DEFAULT 0;
	
