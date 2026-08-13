-- Initial schema for the 45X Portal (Section 7).
-- Written for Azure SQL Database / SQL Server. Column types/constraints here
-- must stay in lockstep with the JPA entity mappings in com.inox.x45.domain.

CREATE TABLE app_user (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    full_name           NVARCHAR(200)  NOT NULL,
    email               NVARCHAR(320)  NOT NULL,
    entra_object_id     NVARCHAR(100)  NULL,
    password_hash       NVARCHAR(255)  NULL,
    active              BIT            NOT NULL DEFAULT 1,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_app_user_email UNIQUE (email)
);
-- Plain UNIQUE constraints in SQL Server reject a second NULL (unlike Postgres/MySQL,
-- which treat each NULL as distinct), and most users won't have an Entra object id
-- until their first login. A filtered index enforces uniqueness only when it's set.
CREATE UNIQUE INDEX uk_app_user_entra_object_id ON app_user(entra_object_id) WHERE entra_object_id IS NOT NULL;

CREATE TABLE app_role (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    name                NVARCHAR(30)   NOT NULL,
    description         NVARCHAR(255)  NULL,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_app_role_name UNIQUE (name)
);

CREATE TABLE user_role_assignment (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id             BIGINT         NOT NULL,
    role_id             BIGINT         NOT NULL,
    assigned_at         DATETIME2      NOT NULL,
    assigned_by_user_id BIGINT         NULL,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_user_role_assignment UNIQUE (user_id, role_id),
    CONSTRAINT fk_ura_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT fk_ura_role FOREIGN KEY (role_id) REFERENCES app_role(id),
    CONSTRAINT fk_ura_assigned_by FOREIGN KEY (assigned_by_user_id) REFERENCES app_user(id)
);
CREATE INDEX ix_user_role_assignment_user ON user_role_assignment(user_id);

CREATE TABLE supplier (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    name                NVARCHAR(200)  NOT NULL,
    country_of_origin   NVARCHAR(100)  NOT NULL,
    feoc_status         NVARCHAR(20)   NOT NULL DEFAULT 'NEEDS_REVIEW',
    feoc_notes          NVARCHAR(1000) NULL,
    material_info       NVARCHAR(1000) NULL,
    active              BIT            NOT NULL DEFAULT 1,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0
);
CREATE INDEX ix_supplier_name ON supplier(name);
CREATE INDEX ix_supplier_country ON supplier(country_of_origin);

CREATE TABLE customer (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    name                NVARCHAR(200)  NOT NULL,
    address             NVARCHAR(500)  NULL,
    contact_name        NVARCHAR(200)  NULL,
    contact_email       NVARCHAR(320)  NULL,
    contact_phone       NVARCHAR(50)   NULL,
    active              BIT            NOT NULL DEFAULT 1,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0
);
CREATE INDEX ix_customer_name ON customer(name);

CREATE TABLE purchase_order (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    po_number           NVARCHAR(50)   NOT NULL,
    supplier_id         BIGINT         NOT NULL,
    order_date          DATE           NOT NULL,
    status              NVARCHAR(30)   NOT NULL DEFAULT 'OPEN',
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_purchase_order_number UNIQUE (po_number),
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id)
);
CREATE INDEX ix_purchase_order_supplier ON purchase_order(supplier_id);
CREATE INDEX ix_purchase_order_date ON purchase_order(order_date);

CREATE TABLE purchase_order_line (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    purchase_order_id   BIGINT         NOT NULL,
    material_code       NVARCHAR(50)   NOT NULL,
    description         NVARCHAR(500)  NULL,
    quantity            DECIMAL(18,3)  NOT NULL,
    uom                 NVARCHAR(20)   NULL,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT fk_pol_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_order(id)
);
CREATE INDEX ix_purchase_order_line_po ON purchase_order_line(purchase_order_id);

CREATE TABLE cell (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    cell_serial_number  NVARCHAR(100)  NOT NULL,
    batch               NVARCHAR(50)   NULL,
    lot                 NVARCHAR(50)   NULL,
    supplier_id         BIGINT         NOT NULL,
    purchase_order_id   BIGINT         NULL,
    wattage             DECIMAL(10,3)  NOT NULL,
    country_of_origin   NVARCHAR(100)  NOT NULL,
    feoc_status         NVARCHAR(20)   NOT NULL DEFAULT 'NEEDS_REVIEW',
    received_date       DATE           NULL,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_cell_serial_number UNIQUE (cell_serial_number),
    CONSTRAINT fk_cell_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id),
    CONSTRAINT fk_cell_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_order(id)
);
CREATE INDEX ix_cell_supplier ON cell(supplier_id);
CREATE INDEX ix_cell_batch_lot ON cell(batch, lot);

CREATE TABLE module (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    module_serial_number NVARCHAR(100) NOT NULL,
    wattage             DECIMAL(10,3)  NOT NULL,
    production_date     DATE           NULL,
    production_line     NVARCHAR(50)   NULL,
    machine_id          NVARCHAR(50)   NULL,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_module_serial_number UNIQUE (module_serial_number)
);
CREATE INDEX ix_module_production_date ON module(production_date);

CREATE TABLE module_cell (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    module_id           BIGINT         NOT NULL,
    cell_id             BIGINT         NOT NULL,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_module_cell_module_cell UNIQUE (module_id, cell_id),
    CONSTRAINT uk_module_cell_cell UNIQUE (cell_id),
    CONSTRAINT fk_mc_module FOREIGN KEY (module_id) REFERENCES module(id),
    CONSTRAINT fk_mc_cell FOREIGN KEY (cell_id) REFERENCES cell(id)
);

CREATE TABLE invoice (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    invoice_number      NVARCHAR(50)   NOT NULL,
    customer_id         BIGINT         NOT NULL,
    invoice_date        DATE           NOT NULL,
    amount              DECIMAL(18,2)  NOT NULL,
    currency            NVARCHAR(3)    NOT NULL DEFAULT 'USD',
    status              NVARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    source              NVARCHAR(10)   NOT NULL DEFAULT 'MANUAL',
    blob_ref            NVARCHAR(500)  NULL,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_invoice_number UNIQUE (invoice_number),
    CONSTRAINT fk_invoice_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
);
CREATE INDEX ix_invoice_customer ON invoice(customer_id);
CREATE INDEX ix_invoice_date ON invoice(invoice_date);
CREATE INDEX ix_invoice_status ON invoice(status);

CREATE TABLE invoice_module (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    invoice_id          BIGINT         NOT NULL,
    module_id           BIGINT         NOT NULL,
    quantity            DECIMAL(10,3)  NOT NULL DEFAULT 1,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_invoice_module UNIQUE (invoice_id, module_id),
    CONSTRAINT fk_im_invoice FOREIGN KEY (invoice_id) REFERENCES invoice(id),
    CONSTRAINT fk_im_module FOREIGN KEY (module_id) REFERENCES module(id)
);

CREATE TABLE document_record (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    document_type       NVARCHAR(30)   NOT NULL,
    blob_ref            NVARCHAR(500)  NOT NULL,
    file_name           NVARCHAR(255)  NOT NULL,
    content_type        NVARCHAR(100)  NOT NULL,
    size_bytes          BIGINT         NOT NULL,
    linked_entity_type  NVARCHAR(30)   NULL,
    linked_entity_id    BIGINT         NULL,
    uploaded_by_user_id BIGINT         NULL,
    uploaded_at         DATETIME2      NOT NULL,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT fk_doc_uploaded_by FOREIGN KEY (uploaded_by_user_id) REFERENCES app_user(id)
);
CREATE INDEX ix_document_record_linked_entity ON document_record(linked_entity_type, linked_entity_id);

CREATE TABLE credit_rate (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    component_type      NVARCHAR(30)   NOT NULL,
    rate_per_watt       DECIMAL(10,6)  NOT NULL,
    effective_from      DATE           NOT NULL,
    effective_to        DATE           NULL,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0
);
CREATE INDEX ix_credit_rate_component_effective ON credit_rate(component_type, effective_from);

CREATE TABLE credit_calculation (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    period              DATE           NOT NULL,
    supplier_id         BIGINT         NULL,
    customer_id         BIGINT         NULL,
    component_type      NVARCHAR(30)   NOT NULL,
    eligible_wattage    DECIMAL(18,3)  NOT NULL,
    rate_per_watt       DECIMAL(10,6)  NOT NULL,
    credit_amount       DECIMAL(18,2)  NOT NULL,
    feoc_status         NVARCHAR(20)   NOT NULL,
    calculated_at       DATETIME2      NOT NULL,
    calculated_by_user_id BIGINT       NULL,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT fk_cc_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id),
    CONSTRAINT fk_cc_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_cc_calculated_by FOREIGN KEY (calculated_by_user_id) REFERENCES app_user(id)
);
CREATE INDEX ix_credit_calculation_period ON credit_calculation(period, supplier_id, customer_id, component_type);

CREATE TABLE feoc_list_entry (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    entry_type          NVARCHAR(20)   NOT NULL,
    name                NVARCHAR(200)  NOT NULL,
    status              NVARCHAR(20)   NOT NULL,
    notes               NVARCHAR(1000) NULL,
    effective_from      DATE           NULL,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_feoc_list_entry UNIQUE (entry_type, name)
);

CREATE TABLE audit_log (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    actor_email         NVARCHAR(320)  NOT NULL,
    action              NVARCHAR(50)   NOT NULL,
    entity_name         NVARCHAR(100)  NOT NULL,
    entity_id           BIGINT         NULL,
    before_json         NVARCHAR(MAX)  NULL,
    after_json          NVARCHAR(MAX)  NULL,
    occurred_at         DATETIME2      NOT NULL,
    correlation_id      NVARCHAR(64)   NULL
);
CREATE INDEX ix_audit_log_entity ON audit_log(entity_name, entity_id);
CREATE INDEX ix_audit_log_occurred_at ON audit_log(occurred_at);

CREATE TABLE material_master (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    material_code       NVARCHAR(50)   NOT NULL,
    description         NVARCHAR(500)  NULL,
    uom                 NVARCHAR(20)   NULL,
    source              NVARCHAR(20)   NOT NULL DEFAULT 'SAP',
    last_synced_at      DATETIME2      NULL,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_material_master_code UNIQUE (material_code)
);

CREATE TABLE production_data_record (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    cell_id             BIGINT         NULL,
    module_id           BIGINT         NULL,
    batch_lot           NVARCHAR(50)   NULL,
    machine_id          NVARCHAR(50)   NULL,
    line_id             NVARCHAR(50)   NULL,
    recorded_at         DATETIME2      NOT NULL,
    raw_payload         NVARCHAR(MAX)  NULL,
    created_at          DATETIME2      NOT NULL,
    updated_at          DATETIME2      NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT fk_pdr_cell FOREIGN KEY (cell_id) REFERENCES cell(id),
    CONSTRAINT fk_pdr_module FOREIGN KEY (module_id) REFERENCES module(id)
);
CREATE INDEX ix_production_data_record_cell ON production_data_record(cell_id);
CREATE INDEX ix_production_data_record_module ON production_data_record(module_id);
