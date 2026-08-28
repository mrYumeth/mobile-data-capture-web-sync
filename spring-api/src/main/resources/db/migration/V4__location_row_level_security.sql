-- =========================================================
-- FieldSync Location Row Level Security
-- Flyway V4
-- =========================================================

ALTER TABLE public.locations
ENABLE ROW LEVEL SECURITY;


DROP POLICY IF EXISTS
    locations_tenant_isolation
ON public.locations;


CREATE POLICY locations_tenant_isolation
ON public.locations
FOR ALL

USING (
    tenant_id =
    public.app_current_tenant_id()
)

WITH CHECK (
    tenant_id =
    public.app_current_tenant_id()
);