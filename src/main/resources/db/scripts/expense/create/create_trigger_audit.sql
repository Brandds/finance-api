DROP TRIGGER IF EXISTS trg_expense_audit ON expense;
CREATE TRIGGER trg_expense_audit
AFTER INSERT OR UPDATE OR DELETE ON expense
FOR EACH ROW EXECUTE FUNCTION audit.log_changes();
