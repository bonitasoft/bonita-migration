-- #############################################################################
-- Before dropping users and databases, we removed sessions associated or connected to them (see -- See https://stackoverflow.com/a/7201350)
-- #############################################################################


-- Ensure we use master database
USE master;
GO


-- Disable login first to prevent any new connections
-- This closes the race condition window
IF EXISTS (SELECT name FROM sys.server_principals WHERE name = '@sqlserver.connection.username@')
BEGIN
  ALTER LOGIN [@sqlserver.connection.username@] DISABLE
END
GO

-- Drop all sessions on all databases that were using this login
DECLARE @kill_all varchar(max) = '';  -- use max to ensure the kill won't fail because we have not allow enough resources
SELECT @kill_all = @kill_all + 'BEGIN TRY KILL ' + CONVERT(varchar(5), session_id) + ' END TRY BEGIN CATCH END CATCH ;' FROM sys.dm_exec_sessions
WHERE login_name = '@sqlserver.connection.username@'

EXEC(@kill_all);
GO

-- Drop login with retry logic
-- Even with disabled login, sessions may take time to terminate
DECLARE @retries INT = 5
DECLARE @dropped BIT = 0

WHILE @retries > 0 AND @dropped = 0
BEGIN
  BEGIN TRY
    IF EXISTS (SELECT name FROM sys.server_principals WHERE name = '@sqlserver.connection.username@')
    BEGIN
      DROP LOGIN [@sqlserver.connection.username@]
      SET @dropped = 1
    END
  END TRY
  BEGIN CATCH
    WAITFOR DELAY '00:00:01'  -- Wait 1 second before retry
    SET @retries = @retries - 1
    IF @retries = 0
    BEGIN
      -- Re-throw the error on final attempt
      THROW
    END
  END CATCH
END
GO

revoke execute on sp_sqljdbc_xa_install to [SqlJDBCXAUser]
GO
revoke execute on xp_sqljdbc_xa_init to [SqlJDBCXAUser]
GO
revoke execute on xp_sqljdbc_xa_start to [SqlJDBCXAUser]
GO
revoke execute on xp_sqljdbc_xa_end to [SqlJDBCXAUser]
GO
revoke execute on xp_sqljdbc_xa_prepare to [SqlJDBCXAUser]
GO
revoke execute on xp_sqljdbc_xa_commit to [SqlJDBCXAUser]
GO
revoke execute on xp_sqljdbc_xa_rollback to [SqlJDBCXAUser]
GO
revoke execute on xp_sqljdbc_xa_recover to [SqlJDBCXAUser]
GO
revoke execute on xp_sqljdbc_xa_forget to [SqlJDBCXAUser]
GO
revoke execute on xp_sqljdbc_xa_rollback_ex to [SqlJDBCXAUser]
GO
revoke execute on xp_sqljdbc_xa_forget_ex to [SqlJDBCXAUser]
GO
revoke execute on xp_sqljdbc_xa_prepare_ex to [SqlJDBCXAUser]
GO
revoke execute on xp_sqljdbc_xa_init_ex to [SqlJDBCXAUser]
GO
EXEC sp_droprolemember 'SqlJDBCXAUser', '@sqlserver.connection.username@'
GO
--    EXEC sp_sqljdbc_xa_uninstall
--GO

-- Drop user in the master database (used for xa) if exists
-- There should not be any sessions associated to this user as it uses the login (we previously removed it and all its associated running sessions)
--IF EXISTS (SELECT name FROM sys.database_principals WHERE name = '@sqlserver.connection.username@')
--BEGIN
    DROP USER @sqlserver.connection.username@;
--END
GO


-- Kill all sessions connected to the database
DECLARE @kill varchar(max) = '';  -- use max to ensure the kill won't fail because we have not allow enough resources
SELECT @kill = @kill + 'BEGIN TRY KILL ' + CONVERT(varchar(5), session_id) + ' END TRY BEGIN CATCH END CATCH ;' FROM sys.dm_exec_sessions
WHERE database_id  = db_id('@sqlserver.db.name@')

EXEC(@kill);
GO


-- Drop database if exists (there should be no error as we have kill all sessions previously connected to it)
IF EXISTS (SELECT name FROM master.dbo.sysdatabases WHERE name = N'@sqlserver.db.name@')
BEGIN
  DROP DATABASE [@sqlserver.db.name@];
END
GO
