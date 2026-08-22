DROP TRIGGER IF EXISTS trg_financial_transaction_audit ON financial_transaction;
CREATE TRIGGER trg_financial_transaction_audit
AFTER INSERT OR UPDATE OR DELETE ON financial_transaction
FOR EACH ROW EXECUTE FUNCTION audit.log_changes();
