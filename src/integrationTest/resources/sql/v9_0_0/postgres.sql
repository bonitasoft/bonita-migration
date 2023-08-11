CREATE TABLE platform (
  id INT8 NOT NULL,
  version VARCHAR(50) NOT NULL,
  initial_bonita_version VARCHAR(50) NOT NULL,
  created INT8 NOT NULL,
  created_by VARCHAR(50) NOT NULL,
  information TEXT,
  PRIMARY KEY (id)
);