ALTER TABLE connector_instance ADD exceptionMessage NVARCHAR(255)
@@
ALTER TABLE connector_instance ADD stackTrace NVARCHAR(MAX)
@@