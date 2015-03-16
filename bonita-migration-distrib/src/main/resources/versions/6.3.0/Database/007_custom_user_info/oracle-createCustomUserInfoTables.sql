CREATE TABLE custom_usr_inf_def (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(75) NOT NULL,
  description VARCHAR2(1024),
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
) @@

ALTER TABLE custom_usr_inf_def ADD CONSTRAINT fk_custom_usr_inf_def_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id) @@

CREATE TABLE custom_usr_inf_val (
  id NUMBER(19, 0) NOT NULL,
  tenantid NUMBER(19, 0) NOT NULL,
  definitionId NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  value VARCHAR2(255),
  UNIQUE (tenantid, definitionId, userId),
  PRIMARY KEY (tenantid, id)
) @@
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_user_id FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE @@
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_definition_id FOREIGN KEY (tenantid, definitionId) REFERENCES custom_usr_inf_def (tenantid, id) ON DELETE CASCADE @@
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_custom_usr_inf_val_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id) @@