CREATE OR REPLACE FUNCTION temporary_content_lo_cleanup()
RETURNS trigger
LANGUAGE plpgsql
AS $func$
BEGIN
  IF OLD.content IS NOT NULL THEN
    PERFORM lo_unlink(OLD.content::oid);
  END IF;
  RETURN OLD;
END;
$func$;

DROP TRIGGER IF EXISTS trg_temporary_content_lo_cleanup ON temporary_content;

CREATE TRIGGER trg_temporary_content_lo_cleanup
AFTER DELETE ON temporary_content
FOR EACH ROW
EXECUTE FUNCTION temporary_content_lo_cleanup();
