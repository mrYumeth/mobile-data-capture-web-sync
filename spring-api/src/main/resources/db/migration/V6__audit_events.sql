-- =========================================================
-- FieldSync Audit Events
-- =========================================================

CREATE SEQUENCE audit_events_id_seq
    AS INTEGER
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE TABLE audit_events (

    id INTEGER NOT NULL
        DEFAULT nextval('audit_events_id_seq'::regclass),

    tenant_id INTEGER NOT NULL,

    actor_user_id INTEGER,

    action VARCHAR(100) NOT NULL,

    entity_type VARCHAR(50) NOT NULL,

    entity_id VARCHAR(100),

    outcome VARCHAR(20) NOT NULL,

    details TEXT,

    created_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,

    CONSTRAINT audit_events_pkey
        PRIMARY KEY (id),

    CONSTRAINT audit_events_tenant_id_fkey
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT audit_events_actor_user_id_fkey
        FOREIGN KEY (actor_user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);


ALTER SEQUENCE audit_events_id_seq
    OWNED BY audit_events.id;


CREATE INDEX idx_audit_events_tenant
    ON audit_events (tenant_id);

CREATE INDEX idx_audit_events_tenant_created
    ON audit_events (tenant_id, created_at);

CREATE INDEX idx_audit_events_action
    ON audit_events (action);


-- =========================================================
-- Tenant isolation
-- =========================================================

ALTER TABLE audit_events
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE audit_events
    FORCE ROW LEVEL SECURITY;


CREATE POLICY audit_events_tenant_isolation
ON audit_events

USING (
    tenant_id =
    NULLIF(
        current_setting(
            'app.current_tenant_id',
            true
        ),
        ''
    )::INTEGER
)

WITH CHECK (
    tenant_id =
    NULLIF(
        current_setting(
            'app.current_tenant_id',
            true
        ),
        ''
    )::INTEGER
);