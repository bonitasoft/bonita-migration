BEGIN
	EXEC sp_rename 'process_instance.startedBy', 'startedFor', 'COLUMN'
	EXEC sp_rename 'process_instance.startedByDelegate', 'startedBy', 'COLUMN'
	
	EXEC sp_rename 'flownode_instance.executedBy', 'executedFor', 'COLUMN'
	EXEC sp_rename 'flownode_instance.executedByDelegate', 'executedBy', 'COLUMN'
END
@@