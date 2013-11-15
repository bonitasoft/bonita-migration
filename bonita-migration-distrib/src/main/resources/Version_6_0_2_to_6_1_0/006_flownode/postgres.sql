--
-- arch_flownode_instance
-- 

ALTER TABLE arch_flownode_instance ADD executedbydelegate INT8;


--
-- flownode_instance
-- 

ALTER TABLE flownode_instance ALTER COLUMN priority TYPE INT2;
ALTER TABLE flownode_instance ADD executedbydelegate INT8;