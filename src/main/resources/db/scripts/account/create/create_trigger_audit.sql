DROP TRIGGER IF EXISTS trg_account_audit ON account;
CREATE TRIGGER trg_account_audit
AFTER INSERT OR UPDATE OR DELETE ON account
FOR EACH ROW EXECUTE FUNCTION audit.log_changes();
