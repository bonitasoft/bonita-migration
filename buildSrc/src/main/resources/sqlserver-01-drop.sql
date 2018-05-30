-- Drop database if exists
IF EXISTS (SELECT name FROM master.dbo.sysdatabases WHERE name = N'@sqlserver.db.name@')
BEGIN
  ALTER DATABASE [@sqlserver.db.name@] SET OFFLINE WITH ROLLBACK IMMEDIATE;
  ALTER DATABASE [@sqlserver.db.name@] SET ONLINE;
  DROP DATABASE [@sqlserver.db.name@];
END
GO

-- Drop user in the master database (used for xa) if exists
IF EXISTS (SELECT name FROM sys.database_principals WHERE name = '@sqlserver.connection.username@')
BEGIN
  DROP USER @sqlserver.connection.username@;
END
GO


-- Drop login if exists
IF EXISTS (SELECT name FROM sys.server_principals WHERE name = '@sqlserver.connection.username@')
BEGIN
  DROP LOGIN @sqlserver.connection.username@
END
GO