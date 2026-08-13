-- Minimal demo master data so the Upload Invoice confirm step (Milestone 4) and
-- traceability chain (Milestone 5+) have at least one customer/supplier to
-- attach records to. Full CRUD for these lands in Milestone 5's Master Data
-- screens - this is just enough to exercise the upload flow end-to-end.

INSERT INTO customer (name, address, contact_name, contact_email, contact_phone, active, created_at, updated_at, version) VALUES
    ('Demo Solar Customer LLC', '1 Solar Way, Austin, TX', 'Jordan Lee', 'jordan.lee@demo-customer.example', '512-555-0100', 1, SYSUTCDATETIME(), SYSUTCDATETIME(), 0);

INSERT INTO supplier (name, country_of_origin, feoc_status, feoc_notes, material_info, active, created_at, updated_at, version) VALUES
    ('Demo Cell Supplier Inc', 'United States', 'PASS', 'Placeholder seed entry.', 'Monocrystalline PERC cells', 1, SYSUTCDATETIME(), SYSUTCDATETIME(), 0);
