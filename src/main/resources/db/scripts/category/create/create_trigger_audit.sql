DROP TRIGGER IF EXISTS trg_category_audit ON category;
CREATE TRIGGER trg_category_audit
AFTER INSERT OR UPDATE OR DELETE ON category
FOR EACH ROW EXECUTE FUNCTION audit.log_changes();
