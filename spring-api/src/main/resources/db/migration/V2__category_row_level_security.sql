-- =========================================================
-- FieldSync Category Row Level Security
-- Flyway V2
--
-- Category RLS is introduced first so tenant isolation can
-- be proven before expanding RLS to other business tables.
-- =========================================================


-- =========================================================
-- Current Tenant Helper
-- =========================================================

CREATE OR REPLACE FUNCTION public.app_current_tenant_id()
RETURNS INTEGER
LANGUAGE sql
STABLE
AS $$
    SELECT NULLIF(
        current_setting(
            'app.current_tenant_id',
            true
        ),
        ''
    )::INTEGER;
$$;


-- =========================================================
-- Categories RLS
-- =========================================================

ALTER TABLE public.categories
ENABLE ROW LEVEL SECURITY;


DROP POLICY IF EXISTS
    categories_tenant_isolation
ON public.categories;


CREATE POLICY categories_tenant_isolation
ON public.categories
FOR ALL

USING (
    tenant_id =
    public.app_current_tenant_id()
)

WITH CHECK (
    tenant_id =
    public.app_current_tenant_id()
);