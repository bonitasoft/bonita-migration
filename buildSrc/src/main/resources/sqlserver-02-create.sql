-- Create database
CREATE DATABASE [@sqlserver.db.name@];
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
IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = '@sqlserver.connection.username@')
BEGIN
  CREATE USER @sqlserver.connection.username@ FOR LOGIN @sqlserver.connection.username@;
END
GO

-- Grant XA permissions
EXEC sp_addrolemember [SqlJDBCXAUser], '@sqlserver.connection.username@';
GO