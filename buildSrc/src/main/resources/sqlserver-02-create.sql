-- Create database
IF NOT EXISTS (SELECT name FROM master.dbo.sysdatabases WHERE name = N'@sqlserver.db.name@')
BEGIN
  CREATE DATABASE [@sqlserver.db.name@];
END
GO

-- Enable Row Versioning-Based Isolation Levels
ALTER DATABASE @sqlserver.db.name@ SET ALLOW_SNAPSHOT_ISOLATION ON;
GO
ALTER DATABASE @sqlserver.db.name@ SET READ_COMMITTED_SNAPSHOT ON;
GO


-- Create login if not exists
IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = '@sqlserver.connection.username@')
BEGIN
  CREATE LOGIN @sqlserver.connection.username@ WITH PASSWORD = '@sqlserver.connection.password@', CHECK_POLICY = OFF;
END
GO


-- Use created database
USE [@sqlserver.db.name@];
GO

-- Create user if not exists
IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = '@sqlserver.connection.username@')
BEGIN
  CREATE USER @sqlserver.connection.username@ FOR LOGIN @sqlserver.connection.username@;
END
GO

-- Grant permissions
EXEC sp_addrolemember N'db_datareader', N'@sqlserver.connection.username@';
EXEC sp_addrolemember N'db_datawriter', N'@sqlserver.connection.username@';
EXEC sp_addrolemember N'db_ddladmin', N'@sqlserver.connection.username@';
GO


--
-- Supporting XA transactions
--
-- Use master database
USE master;
GO

-- Create user if not exists
--IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = '@sqlserver.connection.username@')
--BEGIN
    CREATE USER @sqlserver.connection.username@ FOR LOGIN @sqlserver.connection.username@;
GO

--    EXEC sp_sqljdbc_xa_install
--GO

    -- Grant privileges to [SqlJDBCXAUser] role to the extended stored procedures.
    grant execute on sp_sqljdbc_xa_install to [SqlJDBCXAUser]
GO

    grant execute on xp_sqljdbc_xa_init to [SqlJDBCXAUser]
GO
    grant execute on xp_sqljdbc_xa_start to [SqlJDBCXAUser]
GO
    grant execute on xp_sqljdbc_xa_end to [SqlJDBCXAUser]
GO
    grant execute on xp_sqljdbc_xa_prepare to [SqlJDBCXAUser]
GO
    grant execute on xp_sqljdbc_xa_commit to [SqlJDBCXAUser]
GO
    grant execute on xp_sqljdbc_xa_rollback to [SqlJDBCXAUser]
GO
    grant execute on xp_sqljdbc_xa_recover to [SqlJDBCXAUser]
GO
    grant execute on xp_sqljdbc_xa_forget to [SqlJDBCXAUser]
GO
    grant execute on xp_sqljdbc_xa_rollback_ex to [SqlJDBCXAUser]
GO
    grant execute on xp_sqljdbc_xa_forget_ex to [SqlJDBCXAUser]
GO
    grant execute on xp_sqljdbc_xa_prepare_ex to [SqlJDBCXAUser]
GO
    grant execute on xp_sqljdbc_xa_init_ex to [SqlJDBCXAUser]
GO

    -- Grant XA permissions
    -- Add users to the user [SqlJDBCXAUser] role as needed.
    EXEC sp_addrolemember [SqlJDBCXAUser], '@sqlserver.connection.username@'

--END
GO