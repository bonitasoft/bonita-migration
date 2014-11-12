UPDATE profile
    SET description = 'The user can view and perform tasks and can start a new case of a process.'
    WHERE description = 'The user can view and perform tasks and can start a new case of an app.';

UPDATE profile
    SET description = 'The administrator can install a process, manage the organization, and handle some errors (for example, by replaying a task).'
    WHERE description = 'The administrator can install an app, manage the organization, and handle some errors (for example, by replaying a task).';

UPDATE profile
    SET description = 'The Process manager can supervise designated processes, and manage cases and tasks of those processes.'
    WHERE description = 'The Process manager can supervise designated apps, and manage cases and tasks of those apps.';

UPDATE profileentry
	SET name = 'Processes'
	WHERE name in ('Apps');

UPDATE profileentry
    SET description = 'Manage processes'
    WHERE description in ('Manage apps');

UPDATE profileentry
    SET name = 'Process management'
    WHERE name in ('Apps management');