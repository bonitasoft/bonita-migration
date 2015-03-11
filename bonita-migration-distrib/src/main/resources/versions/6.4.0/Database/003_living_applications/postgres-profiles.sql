UPDATE profile
    SET description = 'The user can view and perform tasks and can start a new case of a process.'
    WHERE name = 'User';

UPDATE profile
    SET description = 'The administrator can install a process, manage the organization, and handle some errors (for example, by replaying a task).'
    WHERE name = 'Administrator';

UPDATE profile
    SET description = 'The Process manager can supervise designated processes, and manage cases and tasks of those processes.'
    WHERE name = 'Process manager';

UPDATE profileentry
	SET name = 'Processes'
	WHERE name = 'Apps';

UPDATE profileentry
    SET description = 'Manage processes'
    WHERE description = 'Manage apps';

UPDATE profileentry
    SET name = 'Process management'
    WHERE name = 'Apps management';