DROP TRIGGER IF EXISTS trg_budget_audit ON budget;
CREATE TRIGGER trg_budget_audit
AFTER INSERT OR UPDATE OR DELETE ON budget
FOR EACH ROW EXECUTE FUNCTION audit.log_changes();
