-- Criar schema audit
CREATE SCHEMA IF NOT EXISTS audit;

-- Criar tabela de log
CREATE TABLE IF NOT EXISTS audit.log (
    id BIGSERIAL PRIMARY KEY,
    table_name TEXT NOT NULL,
    record_id BIGINT,
    action VARCHAR(10) NOT NULL,
    old_data JSONB,
    new_data JSONB,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Criar índices
CREATE INDEX IF NOT EXISTS idx_audit_log_table_name ON audit.log(table_name);
CREATE INDEX IF NOT EXISTS idx_audit_log_record_id ON audit.log(record_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_changed_at ON audit.log(changed_at);

-- Criar função para log de mudanças
CREATE OR REPLACE FUNCTION audit.log_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO audit.log (table_name, record_id, action, new_data)
        VALUES (TG_TABLE_NAME, NEW.id, 'INSERT', to_jsonb(NEW));
        RETURN NEW;

    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO audit.log (table_name, record_id, action, old_data, new_data)
        VALUES (TG_TABLE_NAME, NEW.id, 'UPDATE', to_jsonb(OLD), to_jsonb(NEW));
        RETURN NEW;

    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO audit.log (table_name, record_id, action, old_data)
        VALUES (TG_TABLE_NAME, OLD.id, 'DELETE', to_jsonb(OLD));
        RETURN OLD;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Criar trigger para expense
DROP TRIGGER IF EXISTS trg_audit_expense ON public.expense;
CREATE TRIGGER trg_audit_expense
AFTER INSERT OR UPDATE OR DELETE ON public.expense
FOR EACH ROW
EXECUTE FUNCTION audit.log_changes();

-- Criar trigger para category
DROP TRIGGER IF EXISTS trg_audit_category ON public.category;
CREATE TRIGGER trg_audit_category
AFTER INSERT OR UPDATE OR DELETE ON public.category
FOR EACH ROW
EXECUTE FUNCTION audit.log_changes();

-- Criar trigger para users
DROP TRIGGER IF EXISTS trg_audit_users ON public.users;
CREATE TRIGGER trg_audit_users
AFTER INSERT OR UPDATE OR DELETE ON public.users
FOR EACH ROW
EXECUTE FUNCTION audit.log_changes();
