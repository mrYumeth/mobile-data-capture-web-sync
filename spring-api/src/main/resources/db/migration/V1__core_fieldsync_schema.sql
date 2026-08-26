-- =========================================================
-- FieldSync Core Schema
-- Flyway V1
--
-- Source:
-- database/baseline/supabase-public-schema.sql
--
-- IMPORTANT:
-- Row Level Security and tenant context are intentionally
-- deferred to a later Flyway migration.
-- =========================================================


-- =========================================================
-- Tenants
-- =========================================================

CREATE SEQUENCE tenants_id_seq
    AS INTEGER
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE tenants (
    id INTEGER NOT NULL
        DEFAULT nextval('tenants_id_seq'::regclass),

    name VARCHAR(150) NOT NULL,

    slug VARCHAR(100) NOT NULL,

    is_active BOOLEAN
        DEFAULT TRUE
        NOT NULL,

    created_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT tenants_pkey
        PRIMARY KEY (id),

    CONSTRAINT tenants_slug_key
        UNIQUE (slug)
);

ALTER SEQUENCE tenants_id_seq
    OWNED BY tenants.id;


-- =========================================================
-- Users
-- =========================================================

CREATE SEQUENCE users_id_seq
    AS INTEGER
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE users (
    id INTEGER NOT NULL
        DEFAULT nextval('users_id_seq'::regclass),

    username VARCHAR(100) NOT NULL,

    password_hash TEXT NOT NULL,

    full_name VARCHAR(150),

    role VARCHAR(50)
        DEFAULT 'mobile_user'
        NOT NULL,

    is_active BOOLEAN
        DEFAULT TRUE
        NOT NULL,

    created_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP,

    email VARCHAR(150),

    access_web BOOLEAN
        DEFAULT FALSE
        NOT NULL,

    access_mobile BOOLEAN
        DEFAULT TRUE
        NOT NULL,

    password_change_required BOOLEAN
        DEFAULT TRUE
        NOT NULL,

    confirmation_token TEXT,

    confirmation_expires_at TIMESTAMP WITHOUT TIME ZONE,

    confirmed_at TIMESTAMP WITHOUT TIME ZONE,

    created_by INTEGER,

    tenant_id INTEGER NOT NULL,

    keycloak_user_id TEXT,

    CONSTRAINT users_pkey
        PRIMARY KEY (id),

    CONSTRAINT users_username_key
        UNIQUE (username),

    CONSTRAINT users_created_by_fkey
        FOREIGN KEY (created_by)
        REFERENCES users(id),

    CONSTRAINT users_tenant_id_fkey
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
);

ALTER SEQUENCE users_id_seq
    OWNED BY users.id;


-- =========================================================
-- Customers
-- =========================================================

CREATE SEQUENCE customers_id_seq
    AS INTEGER
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE customers (
    id INTEGER NOT NULL
        DEFAULT nextval('customers_id_seq'::regclass),

    name VARCHAR(150) NOT NULL,

    phone VARCHAR(30),

    email VARCHAR(150),

    address TEXT,

    is_active BOOLEAN
        DEFAULT TRUE,

    created_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP,

    tenant_id INTEGER NOT NULL,

    CONSTRAINT customers_pkey
        PRIMARY KEY (id),

    CONSTRAINT customers_tenant_id_fkey
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
);

ALTER SEQUENCE customers_id_seq
    OWNED BY customers.id;


-- =========================================================
-- Locations
-- =========================================================

CREATE SEQUENCE locations_id_seq
    AS INTEGER
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE locations (
    id INTEGER NOT NULL
        DEFAULT nextval('locations_id_seq'::regclass),

    name VARCHAR(150) NOT NULL,

    address TEXT,

    is_active BOOLEAN
        DEFAULT TRUE,

    created_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP,

    tenant_id INTEGER NOT NULL,

    CONSTRAINT locations_pkey
        PRIMARY KEY (id),

    CONSTRAINT locations_tenant_id_fkey
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
);

ALTER SEQUENCE locations_id_seq
    OWNED BY locations.id;


-- =========================================================
-- Categories
-- =========================================================

CREATE SEQUENCE categories_id_seq
    AS INTEGER
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE categories (
    id INTEGER NOT NULL
        DEFAULT nextval('categories_id_seq'::regclass),

    name VARCHAR(150) NOT NULL,

    description TEXT,

    is_active BOOLEAN
        DEFAULT TRUE,

    created_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP,

    tenant_id INTEGER NOT NULL,

    CONSTRAINT categories_pkey
        PRIMARY KEY (id),

    CONSTRAINT categories_tenant_id_fkey
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
);

ALTER SEQUENCE categories_id_seq
    OWNED BY categories.id;


-- =========================================================
-- Captured Records
-- =========================================================

CREATE SEQUENCE captured_records_id_seq
    AS INTEGER
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE captured_records (
    id INTEGER NOT NULL
        DEFAULT nextval('captured_records_id_seq'::regclass),

    customer_id INTEGER,

    location_id INTEGER,

    category_id INTEGER,

    description TEXT,

    latitude NUMERIC(10,7),

    longitude NUMERIC(10,7),

    image_url TEXT,

    image_path TEXT,

    captured_at TIMESTAMP WITHOUT TIME ZONE,

    received_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP,

    created_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP,

    tenant_id INTEGER NOT NULL,

    CONSTRAINT captured_records_pkey
        PRIMARY KEY (id),

    CONSTRAINT captured_records_customer_id_fkey
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT captured_records_location_id_fkey
        FOREIGN KEY (location_id)
        REFERENCES locations(id),

    CONSTRAINT captured_records_category_id_fkey
        FOREIGN KEY (category_id)
        REFERENCES categories(id),

    CONSTRAINT captured_records_tenant_id_fkey
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
);

ALTER SEQUENCE captured_records_id_seq
    OWNED BY captured_records.id;


-- =========================================================
-- Captured Images
-- =========================================================

CREATE SEQUENCE captured_images_id_seq
    AS INTEGER
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE captured_images (
    id INTEGER NOT NULL
        DEFAULT nextval('captured_images_id_seq'::regclass),

    captured_record_id INTEGER,

    image_url TEXT,

    storage_path TEXT,

    created_at TIMESTAMP WITHOUT TIME ZONE
        DEFAULT CURRENT_TIMESTAMP,

    tenant_id INTEGER NOT NULL,

    CONSTRAINT captured_images_pkey
        PRIMARY KEY (id),

    CONSTRAINT captured_images_captured_record_id_fkey
        FOREIGN KEY (captured_record_id)
        REFERENCES captured_records(id)
        ON DELETE CASCADE,

    CONSTRAINT captured_images_tenant_id_fkey
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
);

ALTER SEQUENCE captured_images_id_seq
    OWNED BY captured_images.id;


-- =========================================================
-- Tenant indexes
-- =========================================================

CREATE INDEX idx_users_tenant_id
    ON users (tenant_id);

CREATE INDEX idx_customers_tenant_id
    ON customers (tenant_id);

CREATE INDEX idx_locations_tenant_id
    ON locations (tenant_id);

CREATE INDEX idx_categories_tenant_id
    ON categories (tenant_id);

CREATE INDEX idx_captured_records_tenant_id
    ON captured_records (tenant_id);

CREATE INDEX idx_captured_records_tenant_customer
    ON captured_records (tenant_id, customer_id);

CREATE INDEX idx_captured_records_tenant_location
    ON captured_records (tenant_id, location_id);

CREATE INDEX idx_captured_records_tenant_category
    ON captured_records (tenant_id, category_id);

CREATE INDEX idx_captured_images_tenant_id
    ON captured_images (tenant_id);


-- =========================================================
-- Authentication / Identity indexes
-- =========================================================

CREATE UNIQUE INDEX idx_users_keycloak_user_id
    ON users (keycloak_user_id)
    WHERE keycloak_user_id IS NOT NULL;

CREATE UNIQUE INDEX users_email_lower_unique_idx
    ON users (LOWER(email))
    WHERE email IS NOT NULL;

CREATE UNIQUE INDEX users_username_lower_unique_idx
    ON users (LOWER(username));