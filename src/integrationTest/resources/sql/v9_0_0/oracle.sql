CREATE TABLE platform (
  id NUMBER(19, 0) NOT NULL,
  version VARCHAR2(50 CHAR) NOT NULL,
  initial_bonita_version VARCHAR2(50 CHAR) NOT NULL,
  created NUMBER(19, 0) NOT NULL,
  created_by VARCHAR2(50 CHAR) NOT NULL,
  information CLOB,
  PRIMARY KEY (id)
);