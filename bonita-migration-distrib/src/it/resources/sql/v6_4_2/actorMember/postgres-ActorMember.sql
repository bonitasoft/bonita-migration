CREATE TABLE actor (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  scopeId INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(75),
  description TEXT,
  initiator BOOLEAN,
  UNIQUE (tenantid, id, scopeId, name),
  PRIMARY KEY (tenantid, id)
)
@@
CREATE TABLE actormember (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  actorId INT8 NOT NULL,
  userId INT8 NOT NULL,
  groupId INT8 NOT NULL,
  roleId INT8 NOT NULL,
  UNIQUE (tenantid, actorid, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
)
@@
ALTER TABLE actormember ADD CONSTRAINT fk_actormember_actorId FOREIGN KEY (tenantid, actorId) REFERENCES actor(tenantid, id)

