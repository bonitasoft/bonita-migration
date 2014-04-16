BEGIN
	EXEC sp_rename 'process_instance.startedByDelegate', 'startedBySubstitute', 'COLUMN'
	
	EXEC sp_rename 'flownode_instance.executedByDelegate', 'executedBySubstitute', 'COLUMN'
END
@@