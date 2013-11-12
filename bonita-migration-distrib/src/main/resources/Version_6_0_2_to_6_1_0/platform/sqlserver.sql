UPDATE platform SET previousVersion = version
GO
UPDATE platform SET version = :version
GO