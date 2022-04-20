CREATE TABLE profileentry (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  profileId BIGINT NOT NULL,
  name VARCHAR(50),
  description TEXT,
  parentId BIGINT,
  index_ BIGINT,
  type VARCHAR(50),
  page VARCHAR(50),
  custom BOOLEAN DEFAULT FALSE,
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB