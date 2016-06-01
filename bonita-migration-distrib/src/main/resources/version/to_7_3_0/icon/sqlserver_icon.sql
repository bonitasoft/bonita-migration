CREATE TABLE icon (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  mimetype NVARCHAR(255) NOT NULL,
  content VARBINARY(MAX) NOT NULL,
  CONSTRAINT pk_icon PRIMARY KEY (tenantId, id)
)
GO
ALTER TABLE user_ add iconid NUMERIC(19, 0)
GO
ALTER TABLE group_ add iconid NUMERIC(19, 0)
GO
ALTER TABLE role add iconid NUMERIC(19, 0)
GO