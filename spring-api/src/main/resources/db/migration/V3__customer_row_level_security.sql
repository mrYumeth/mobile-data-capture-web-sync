-- =========================================================
-- FieldSync Customer Row Level Security
-- Flyway V3
-- =========================================================

ALTER TABLE public.customers
ENABLE ROW LEVEL SECURITY;


DROP POLICY IF EXISTS
    customers_tenant_isolation
ON public.customers;


CREATE POLICY customers_tenant_isolation
ON public.customers
FOR ALL

USING (
    tenant_id =
    public.app_current_tenant_id()
)

WITH CHECK (
    tenant_id =
    public.app_current_tenant_id()
);