CREATE TABLE profileentry (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  profileId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50),
  description NVARCHAR(MAX),
  parentId NUMERIC(19, 0),
  index_ NUMERIC(19, 0),
  type NVARCHAR(50),
  page NVARCHAR(255),
  custom BIT DEFAULT 0,
  PRIMARY KEY (tenantId, id)
)