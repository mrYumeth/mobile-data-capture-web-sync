-- =========================================================
-- FieldSync Captured Record Row Level Security
-- Flyway V5
--
-- Protect both captured records and their images using the
-- trusted tenant context established by Spring.
-- =========================================================


-- =========================================================
-- Captured Records
-- =========================================================

ALTER TABLE public.captured_records
ENABLE ROW LEVEL SECURITY;


DROP POLICY IF EXISTS
    captured_records_tenant_isolation
ON public.captured_records;


CREATE POLICY captured_records_tenant_isolation
ON public.captured_records
FOR ALL

USING (
    tenant_id =
    public.app_current_tenant_id()
)

WITH CHECK (
    tenant_id =
    public.app_current_tenant_id()
);


-- =========================================================
-- Captured Images
-- =========================================================

ALTER TABLE public.captured_images
ENABLE ROW LEVEL SECURITY;


DROP POLICY IF EXISTS
    captured_images_tenant_isolation
ON public.captured_images;


CREATE POLICY captured_images_tenant_isolation
ON public.captured_images
FOR ALL

USING (
    tenant_id =
    public.app_current_tenant_id()
)

WITH CHECK (
    tenant_id =
    public.app_current_tenant_id()
);