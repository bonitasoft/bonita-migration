CREATE TABLE custom_usr_inf_def (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(75) NOT NULL,
  description TEXT,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE INDEX idx_custom_usr_inf_def_name ON custom_usr_inf_def (tenantid, name);
CREATE INDEX fk_custom_usr_inf_def_tenantId_idx ON custom_usr_inf_def(tenantid ASC);
ALTER TABLE custom_usr_inf_def ADD CONSTRAINT fk_custom_usr_inf_def_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);


CREATE TABLE custom_usr_inf_val (
  id BIGINT NOT NULL,
  tenantid BIGINT NOT NULL,
  definitionId BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  value VARCHAR(255),
  UNIQUE (tenantid, definitionId, userId),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX fk_custom_usr_inf_val_tenantId_idx ON custom_usr_inf_val(tenantid ASC);
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_custom_usr_inf_val_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_definition_id FOREIGN KEY (tenantid, definitionId) REFERENCES custom_usr_inf_def (tenantid, id) ON DELETE CASCADE;
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_user_id FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;
