CREATE TABLE application_settings (
    id UUID PRIMARY KEY,
    setting_key VARCHAR(255) NOT NULL UNIQUE,
    setting_value TEXT,
    updated_at TIMESTAMP
);

CREATE TABLE cable_types (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE device_types (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE installation_materials (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    unit VARCHAR(64)
);

CREATE TABLE mounting_elements (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE project_customers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    enterprise_name VARCHAR(255),
    tax_number VARCHAR(255),
    contact_email VARCHAR(255),
    created_at TIMESTAMP
);

CREATE TABLE project_customer_phones (
    customer_id UUID NOT NULL,
    phone_number VARCHAR(64) NOT NULL,
    PRIMARY KEY (customer_id, phone_number),
    CONSTRAINT fk_project_customer_phones_customer
        FOREIGN KEY (customer_id) REFERENCES project_customers(id) ON DELETE CASCADE
);

CREATE TABLE user_accounts (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES user_accounts(id) ON DELETE CASCADE
);

CREATE TABLE database_connections (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    host VARCHAR(255),
    port INTEGER,
    database_name VARCHAR(255),
    username VARCHAR(255),
    password VARCHAR(255),
    database_type VARCHAR(255),
    initialized BOOLEAN NOT NULL DEFAULT FALSE,
    last_verified_at TIMESTAMP,
    status_message VARCHAR(1024),
    active BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE managed_objects (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2048),
    primary_data TEXT,
    primary_data_version INTEGER DEFAULT 1,
    customer_id UUID NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deletion_requested BOOLEAN NOT NULL DEFAULT FALSE,
    deletion_requested_at TIMESTAMP,
    deletion_requested_by UUID,
    CONSTRAINT fk_managed_objects_customer
        FOREIGN KEY (customer_id) REFERENCES project_customers(id),
    CONSTRAINT fk_managed_objects_deletion_user
        FOREIGN KEY (deletion_requested_by) REFERENCES user_accounts(id) ON DELETE SET NULL
);

CREATE TABLE stored_files (
    id UUID PRIMARY KEY,
    original_filename VARCHAR(255),
    stored_filename VARCHAR(255),
    size BIGINT NOT NULL,
    uploaded_at TIMESTAMP,
    content_type VARCHAR(255),
    object_id UUID,
    CONSTRAINT fk_stored_files_object
        FOREIGN KEY (object_id) REFERENCES managed_objects(id) ON DELETE CASCADE
);

CREATE TABLE object_changes (
    id UUID PRIMARY KEY,
    object_id UUID,
    user_id UUID,
    changed_at TIMESTAMP,
    change_type VARCHAR(255),
    field_name VARCHAR(255),
    old_value TEXT,
    new_value TEXT,
    summary VARCHAR(1024),
    CONSTRAINT fk_object_changes_object
        FOREIGN KEY (object_id) REFERENCES managed_objects(id) ON DELETE CASCADE,
    CONSTRAINT fk_object_changes_user
        FOREIGN KEY (user_id) REFERENCES user_accounts(id) ON DELETE SET NULL
);

CREATE TABLE device_cable_profiles (
    id UUID PRIMARY KEY,
    device_type_id UUID NOT NULL,
    cable_type_id UUID NOT NULL,
    endpoint_name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_device_cable_profiles_device_type
        FOREIGN KEY (device_type_id) REFERENCES device_types(id) ON DELETE CASCADE,
    CONSTRAINT fk_device_cable_profiles_cable_type
        FOREIGN KEY (cable_type_id) REFERENCES cable_types(id) ON DELETE CASCADE
);

