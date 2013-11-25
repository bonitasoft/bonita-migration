--
-- arch_flownode_instance
-- 

ALTER TABLE arch_flownode_instance ADD executedByDelegate BIGINT(20);


--
-- flownode_instance
-- 

ALTER TABLE flownode_instance MODIFY COLUMN priority TINYINT(4) NULL;
ALTER TABLE flownode_instance ADD executedByDelegate BIGINT(20);


--
-- Datas
-- 

UPDATE flownode_instance SET executedbydelegate = executedby;
UPDATE arch_flownode_instance SET executedbydelegate = executedby;