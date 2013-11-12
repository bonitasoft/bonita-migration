ALTER TABLE connector_instance ADD exceptionMessage NVARCHAR(255)
GO
ALTER TABLE connector_instance ADD stackTrace NVARCHAR(MAX)
GO