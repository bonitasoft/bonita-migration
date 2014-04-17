BEGIN
	EXEC sp_rename 'process_instance.startedByDelegate', 'startedBySubstitute', 'COLUMN'
	EXEC sp_rename 'arch_process_instance.startedByDelegate', 'startedBySubstitute', 'COLUMN'
	
	EXEC sp_rename 'flownode_instance.executedByDelegate', 'executedBySubstitute', 'COLUMN'
	EXEC sp_rename 'arch_flownode_instance.executedByDelegate', 'executedBySubstitute', 'COLUMN'
END
@@