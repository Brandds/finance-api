DROP TRIGGER IF EXISTS trg_expense_tag_audit ON expense_tag;
CREATE TRIGGER trg_expense_tag_audit
AFTER INSERT OR UPDATE OR DELETE ON expense_tag
FOR EACH ROW EXECUTE FUNCTION audit.log_changes();
