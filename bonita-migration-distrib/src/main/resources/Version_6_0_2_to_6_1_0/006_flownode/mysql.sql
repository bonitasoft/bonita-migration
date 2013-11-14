ALTER TABLE arch_flownode_instance ADD executedByDelegate BIGINT(20);
ALTER TABLE flownode_instance MODIFY COLUMN priority TINYINT(4) NULL;
ALTER TABLE flownode_instance ADD executedByDelegate BIGINT(20);