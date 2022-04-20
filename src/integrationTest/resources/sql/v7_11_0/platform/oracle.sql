CREATE TABLE platform (
    id              NUMBER(19, 0) NOT NULL,
    version         VARCHAR2(50 CHAR) NOT NULL,
    previousVersion VARCHAR2(50 CHAR),
    initialVersion  VARCHAR2(50 CHAR) NOT NULL,
    created         NUMBER(19, 0) NOT NULL,
    createdBy       VARCHAR2(50 CHAR) NOT NULL,
    information     CLOB,
    PRIMARY KEY (id)
);