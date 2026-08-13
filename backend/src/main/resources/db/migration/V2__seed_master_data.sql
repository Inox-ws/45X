-- Seed data for local development and demos. Credit rates below are
-- PLACEHOLDER VALUES for scaffolding only - Section 6.1 requires these to be
-- verified against current IRS/Treasury 45X guidance before any real use;
-- an admin must review and correct them via Master Data (Milestone 5) before
-- this environment is used for anything but demoing the traceability flow.

INSERT INTO app_role (name, description, created_at, updated_at, version) VALUES
    ('FINANCE',    'Upload invoices; view credits & reports',            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('LOGISTICS',  'Upload POD; view shipments',                          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('PRODUCTION', 'Traceability; view production data',                  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('MANAGEMENT', 'Dashboard; analytics & reports (read-only)',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('ADMIN',      'User management; master data; full access',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Local-dev fallback admin user (Section 3), 'local' profile only. Password is
-- "ChangeMe123!" - a BCrypt hash of a fixed demo password committed to source
-- control, which is fine ONLY because this account is unreachable outside the
-- 'local' profile (no Entra ID dependency, H2 in-memory DB). Never reuse this
-- pattern for the 'azure' profile or any real user.
INSERT INTO app_user (full_name, email, entra_object_id, password_hash, active, created_at, updated_at, version) VALUES
    ('Demo Admin', 'demo.admin@example.com', NULL, '$2b$10$UsbI7e4EIWwcZ5y12f0W8eiawpgugFKE6rYCdQVv/fnIaxLudPUL6', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO user_role_assignment (user_id, role_id, assigned_at, assigned_by_user_id, created_at, updated_at, version)
SELECT u.id, r.id, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM app_user u, app_role r
WHERE u.email = 'demo.admin@example.com' AND r.name = 'ADMIN';

-- PLACEHOLDER 45X credit rates - replace with verified IRS/Treasury figures.
INSERT INTO credit_rate (component_type, rate_per_watt, effective_from, effective_to, created_at, updated_at, version) VALUES
    ('SOLAR_CELL',   0.04, '2023-01-01', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('SOLAR_MODULE', 0.07, '2023-01-01', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Starter FEOC/PFE list - not exhaustive, needs admin review (Master Data, Milestone 5).
INSERT INTO feoc_list_entry (entry_type, name, status, notes, effective_from, created_at, updated_at, version) VALUES
    ('COUNTRY', 'China',       'PROHIBITED', 'Placeholder seed entry - confirm against current FEOC guidance.', '2023-01-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('COUNTRY', 'North Korea', 'PROHIBITED', 'Placeholder seed entry - confirm against current FEOC guidance.', '2023-01-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('COUNTRY', 'Russia',      'PROHIBITED', 'Placeholder seed entry - confirm against current FEOC guidance.', '2023-01-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('COUNTRY', 'Iran',        'PROHIBITED', 'Placeholder seed entry - confirm against current FEOC guidance.', '2023-01-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
