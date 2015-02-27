--
-- Archived_transition_instance
-- 

ALTER TABLE arch_transition_instance ADD name_temp VARCHAR2(255 CHAR) @@
UPDATE arch_transition_instance SET name_temp = name @@
ALTER TABLE arch_transition_instance DROP COLUMN name @@
ALTER TABLE arch_transition_instance RENAME COLUMN name_temp TO name @@
ALTER TABLE arch_transition_instance MODIFY name NOT NULL @@


ALTER TABLE arch_transition_instance ADD state_temp VARCHAR2(50 CHAR) @@
UPDATE arch_transition_instance SET state_temp = state @@
ALTER TABLE arch_transition_instance DROP COLUMN state @@
ALTER TABLE arch_transition_instance RENAME COLUMN state_temp TO state @@


ALTER TABLE arch_transition_instance ADD stateCategory_temp VARCHAR2(50 CHAR) @@
UPDATE arch_transition_instance SET stateCategory_temp = stateCategory @@
ALTER TABLE arch_transition_instance DROP COLUMN stateCategory @@
ALTER TABLE arch_transition_instance RENAME COLUMN stateCategory_temp TO stateCategory @@
ALTER TABLE arch_transition_instance MODIFY stateCategory NOT NULL @@


ALTER TABLE arch_transition_instance ADD description_temp VARCHAR2(255 CHAR) @@
UPDATE arch_transition_instance SET description_temp = description @@
ALTER TABLE arch_transition_instance DROP COLUMN description @@
ALTER TABLE arch_transition_instance RENAME COLUMN description_temp TO description @@
