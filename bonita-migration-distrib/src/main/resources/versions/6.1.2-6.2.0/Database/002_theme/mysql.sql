CREATE TABLE theme (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  isDefault BOOLEAN NOT NULL,
  content LONGBLOB NOT NULL,
  cssContent LONGBLOB,
  type VARCHAR(50) NOT NULL,
  lastUpdateDate BIGINT NOT NULL,
  CONSTRAINT `UK_Theme` UNIQUE (tenantId, isDefault, type),
  PRIMARY KEY (tenantId, id)
)ENGINE = INNODB;

CREATE INDEX fk_theme_tenantId_idx ON theme (tenantid ASC);

INSERT INTO sequence (tenantid, id, nextid)
SELECT ID, 9890, 1 FROM tenant
ORDER BY id ASC;
ALTER TABLE theme ADD CONSTRAINT fk_theme_tenantId FOREIGN KEY (tenantId) REFERENCES tenant (id)
                                                             ON DELETE NO ACTION
                                                             ON UPDATE NO ACTION;