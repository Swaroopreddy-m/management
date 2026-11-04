-- Drop tables if they exist to start fresh
DROP TABLE IF EXISTS user_id_mapping CASCADE;
DROP TABLE IF EXISTS pin_master CASCADE;
DROP TABLE IF EXISTS user_master CASCADE;
DROP TABLE IF EXISTS EMPLOYEE CASCADE;

-- 1. Create EMPLOYEE Table
CREATE TABLE EMPLOYEE (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255),
    department VARCHAR(255),
    salary DOUBLE PRECISION,
    organization_id VARCHAR(64) DEFAULT 'ORG001',
    entity_id VARCHAR(64) DEFAULT 'BR001'
);

-- 2. Create user_master Table
CREATE TABLE user_master (
    CUSTOMER_ID VARCHAR(255) PRIMARY KEY,
    NAME VARCHAR(255),
    EMAIL VARCHAR(200),
    MOBILE VARCHAR(200),
    SYSTEM_USER_ID VARCHAR(100) UNIQUE,
    USER_NAME VARCHAR(200),
    USER_GROUPS VARCHAR(100),
    USER_ENTITY VARCHAR(100),
    USER_STATUS VARCHAR(100),
    USER_LAST_LOGIN_TIME VARCHAR(100),
    CREATED_BY VARCHAR(100),
    CREATED_TIME VARCHAR(100),
    APPROVED_BY VARCHAR(100),
    APPROVED_TIME VARCHAR(100),
    IP VARCHAR(100),
    OTHERS VARCHAR(100),
    ORGANIZATION_ID VARCHAR(64) DEFAULT 'ORG001',
    USER_ROLE VARCHAR(50) DEFAULT 'ORG_ADMIN'
);

-- 3. Create pin_master Table
CREATE TABLE pin_master (
    CUSTOMER_ID VARCHAR(255) PRIMARY KEY,
    CUSTOMER_PASSWORD VARCHAR(255),
    SYSTEM_USER_ID VARCHAR(100)
);

-- 4. Create user_id_mapping Table
CREATE TABLE user_id_mapping (
    id BIGSERIAL PRIMARY KEY,
    SYSTEM_USER_ID VARCHAR(100),
    LOGIN_USER_ID VARCHAR(100),
    CREATED_BY VARCHAR(100),
    CREATED_TIME VARCHAR(100),
    APPROVED_BY VARCHAR(100),
    APPROVED_TIME VARCHAR(100),
    IP VARCHAR(100),
    OTHERS VARCHAR(100),
    CONSTRAINT fk_user_master_system FOREIGN KEY (SYSTEM_USER_ID) REFERENCES user_master(SYSTEM_USER_ID)
);

-- 5. Seed Data
-- Seed Employees
INSERT INTO EMPLOYEE (name, email, department, salary, organization_id, entity_id) VALUES
('Alice Johnson', 'alice@example.com', 'Engineering', 75000.00, 'ORG001', 'BR001'),
('Bob Smith', 'bob@example.com', 'Product', 82000.00, 'ORG001', 'BR001'),
('Carol White', 'carol@example.com', 'HR', 60000.00, 'ORG001', 'BR002');

-- Seed User Master
INSERT INTO user_master (
    CUSTOMER_ID, NAME, EMAIL, MOBILE, SYSTEM_USER_ID, USER_NAME, USER_GROUPS, 
    USER_ENTITY, USER_STATUS, USER_LAST_LOGIN_TIME, CREATED_BY, CREATED_TIME, 
    APPROVED_BY, APPROVED_TIME, IP, OTHERS, ORGANIZATION_ID, USER_ROLE
) VALUES (
    'US001', 'John Doe', 'john.doe@example.com', '9876543210', 'SYS001', 'john_doe', 'ADMIN',
    'ENT01', 'ACTIVE', '2026-07-07 10:00:00', 'system', '2026-07-07 09:00:00',
    'supervisor', '2026-07-07 09:30:00', '127.0.0.1', 'none', 'ORG001', 'ORG_ADMIN'
);

-- Seed Pin Master (using matching CUSTOMER_ID)
INSERT INTO pin_master (CUSTOMER_ID, CUSTOMER_PASSWORD, SYSTEM_USER_ID) VALUES
('US001', 'password123', 'SYS001');

-- Seed User ID Mapping
INSERT INTO user_id_mapping (SYSTEM_USER_ID, LOGIN_USER_ID, CREATED_BY, CREATED_TIME, APPROVED_BY, APPROVED_TIME, IP, OTHERS) VALUES
('SYS001', 'john_doe', 'system', '2026-07-07 09:00:00', 'supervisor', '2026-07-07 09:30:00', '127.0.0.1', 'none');

-- ============================================================================
-- 6. Hierarchical Multi-Organization Tables
-- ============================================================================

-- 6.1 Super Entity Table
CREATE TABLE IF NOT EXISTS super_entities (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6.2 Super Admin Table
CREATE TABLE IF NOT EXISTS super_admins (
    id VARCHAR(64) PRIMARY KEY,
    super_entity_id VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(200) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_super_admin_entity FOREIGN KEY (super_entity_id) REFERENCES super_entities(id) ON DELETE CASCADE
);

-- 6.3 Organization Table
CREATE TABLE IF NOT EXISTS organizations (
    id VARCHAR(64) PRIMARY KEY,
    super_entity_id VARCHAR(64) NOT NULL,
    super_admin_id VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    subscription_tier VARCHAR(50) DEFAULT 'STANDARD',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_org_super_entity FOREIGN KEY (super_entity_id) REFERENCES super_entities(id),
    CONSTRAINT fk_org_super_admin FOREIGN KEY (super_admin_id) REFERENCES super_admins(id)
);

-- 6.4 Organizational Entities (Branches, Departments, Stores)
CREATE TABLE IF NOT EXISTS org_entities (
    id VARCHAR(64) PRIMARY KEY,
    organization_id VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_org_entity_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- 6.5 Dynamic Page Registry
CREATE TABLE IF NOT EXISTS page_registry (
    page_id VARCHAR(64) PRIMARY KEY,
    page_name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    route VARCHAR(255) NOT NULL,
    category VARCHAR(100) DEFAULT 'CORE',
    status VARCHAR(50) NOT NULL DEFAULT 'REGISTERED',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6.6 Page Actions
CREATE TABLE IF NOT EXISTS page_actions (
    id BIGSERIAL PRIMARY KEY,
    page_id VARCHAR(64) NOT NULL,
    action_code VARCHAR(32) NOT NULL,
    action_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uq_page_action UNIQUE (page_id, action_code),
    CONSTRAINT fk_action_page FOREIGN KEY (page_id) REFERENCES page_registry(page_id) ON DELETE CASCADE
);

-- 6.7 Organization Page Permissions (Super Admin approvals)
CREATE TABLE IF NOT EXISTS org_page_permissions (
    id BIGSERIAL PRIMARY KEY,
    organization_id VARCHAR(64) NOT NULL,
    page_id VARCHAR(64) NOT NULL,
    allowed_actions VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    granted_by VARCHAR(100),
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP,
    revocation_reason VARCHAR(500),
    CONSTRAINT uq_org_page UNIQUE (organization_id, page_id),
    CONSTRAINT fk_perm_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_perm_page FOREIGN KEY (page_id) REFERENCES page_registry(page_id) ON DELETE CASCADE
);

-- 6.8 Permission Groups
CREATE TABLE IF NOT EXISTS permission_groups (
    id VARCHAR(64) PRIMARY KEY,
    organization_id VARCHAR(64) NOT NULL,
    group_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_org_group UNIQUE (organization_id, group_name),
    CONSTRAINT fk_grp_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- 6.9 Group Permissions (Action assignments to groups)
CREATE TABLE IF NOT EXISTS group_permissions (
    id BIGSERIAL PRIMARY KEY,
    group_id VARCHAR(64) NOT NULL,
    organization_id VARCHAR(64) NOT NULL,
    page_id VARCHAR(64) NOT NULL,
    permitted_actions VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_group_page UNIQUE (group_id, page_id),
    CONSTRAINT fk_gp_group FOREIGN KEY (group_id) REFERENCES permission_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_gp_page FOREIGN KEY (page_id) REFERENCES page_registry(page_id) ON DELETE CASCADE
);

-- 6.10 User Group Assignments
CREATE TABLE IF NOT EXISTS user_group_assignments (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    organization_id VARCHAR(64) NOT NULL,
    group_id VARCHAR(64) NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_group UNIQUE (user_id, group_id),
    CONSTRAINT fk_uga_group FOREIGN KEY (group_id) REFERENCES permission_groups(id) ON DELETE CASCADE
);

-- 6.11 User Entity Assignments
CREATE TABLE IF NOT EXISTS user_entity_assignments (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    organization_id VARCHAR(64) NOT NULL,
    entity_id VARCHAR(64) NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_entity UNIQUE (user_id, entity_id),
    CONSTRAINT fk_uea_entity FOREIGN KEY (entity_id) REFERENCES org_entities(id) ON DELETE CASCADE
);

-- 6.12 Page Access Requests (Workflow)
CREATE TABLE IF NOT EXISTS page_access_requests (
    id VARCHAR(64) PRIMARY KEY,
    organization_id VARCHAR(64) NOT NULL,
    requested_page_id VARCHAR(64) NOT NULL,
    requested_actions VARCHAR(255) DEFAULT 'VIEW',
    justification VARCHAR(1000),
    requesting_user_id VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    decision_reason VARCHAR(1000),
    reviewed_by VARCHAR(100),
    decision_timestamp TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_req_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- 6.13 Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_id VARCHAR(100),
    actor_name VARCHAR(255),
    actor_role VARCHAR(50),
    organization_id VARCHAR(64),
    entity_id VARCHAR(64),
    action VARCHAR(100) NOT NULL,
    target_resource VARCHAR(255),
    outcome VARCHAR(32) NOT NULL,
    details VARCHAR(2000),
    ip_address VARCHAR(100),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- 7. Seed Hierarchy & Dynamic Page Data
-- ============================================================================

-- Super Entity
INSERT INTO super_entities (id, name, code, description) VALUES
('SE001', 'Global Enterprise Group', 'SE_GLOBAL', 'Top-level enterprise holding entity')
ON CONFLICT (id) DO NOTHING;

-- Super Admin
INSERT INTO super_admins (id, super_entity_id, name, email, username, password_hash) VALUES
('SA001', 'SE001', 'Enterprise Super Admin', 'superadmin@global.com', 'superadmin', 'admin123')
ON CONFLICT (id) DO NOTHING;

-- Organizations
INSERT INTO organizations (id, super_entity_id, super_admin_id, name, code, subscription_tier) VALUES
('ORG001', 'SE001', 'SA001', 'Apex Solutions', 'ORG_APEX', 'ENTERPRISE'),
('ORG002', 'SE001', 'SA001', 'Beta Industries', 'ORG_BETA', 'STANDARD')
ON CONFLICT (id) DO NOTHING;

-- Organizational Entities (Branches, Departments, Stores)
INSERT INTO org_entities (id, organization_id, entity_type, name, code) VALUES
('BR001', 'ORG001', 'BRANCH', 'Apex Headquarters', 'BR_HQ'),
('BR002', 'ORG001', 'BRANCH', 'Apex West Coast', 'BR_WEST'),
('DEPT001', 'ORG001', 'DEPARTMENT', 'Engineering Dept', 'DEP_ENG'),
('STR001', 'ORG001', 'STORE', 'Main Fulfillment Store', 'STR_MAIN'),
('BR003', 'ORG002', 'BRANCH', 'Beta Metro Hub', 'BR_METRO')
ON CONFLICT (id) DO NOTHING;

-- Standard Pages in Registry
INSERT INTO page_registry (page_id, page_name, description, route, category, status, is_active) VALUES
('DASHBOARD', 'Main Dashboard', 'Central executive metrics and summary overview', '/dashboard', 'Core', 'ACTIVE', TRUE),
('EMPLOYEE_MANAGEMENT', 'Employee Directory', 'Manage organizational employees, salaries, and departments', '/dashboard/employees', 'Operations', 'ACTIVE', TRUE),
('USER_MANAGEMENT', 'User Administration', 'System user profiles, roles, and credential management', '/dashboard/users', 'Administration', 'ACTIVE', TRUE),
('BRANCH_MANAGEMENT', 'Branch Management', 'Manage geographical branches and operational facilities', '/dashboard/branches', 'Organization', 'ACTIVE', TRUE),
('DEPARTMENT_MANAGEMENT', 'Department Management', 'Manage internal functional divisions and departments', '/dashboard/departments', 'Organization', 'ACTIVE', TRUE),
('STORE_MANAGEMENT', 'Store Management', 'Manage retail and inventory outlets', '/dashboard/stores', 'Organization', 'ACTIVE', TRUE),
('REPORTS', 'Analytics & Reports', 'Exportable business analytics and performance reporting', '/dashboard/reports', 'Analytics', 'ACTIVE', TRUE),
('SETTINGS', 'Organization Settings', 'General system preferences and metadata configurations', '/dashboard/settings', 'Settings', 'ACTIVE', TRUE),
('DYNAMIC_PAGES', 'Dynamic Layout Builder', 'Configurable capture, confirm, and acknowledge form builder', '/page/capture', 'Core', 'ACTIVE', TRUE)
ON CONFLICT (page_id) DO NOTHING;

-- Page Actions
INSERT INTO page_actions (page_id, action_code, action_name, description) VALUES
('DASHBOARD', 'VIEW', 'View Dashboard', 'Access dashboard metrics'),
('EMPLOYEE_MANAGEMENT', 'VIEW', 'View Employees', 'Read employee records'),
('EMPLOYEE_MANAGEMENT', 'CREATE', 'Create Employee', 'Add new employees'),
('EMPLOYEE_MANAGEMENT', 'UPDATE', 'Update Employee', 'Modify employee records'),
('EMPLOYEE_MANAGEMENT', 'DELETE', 'Delete Employee', 'Remove employee records'),
('EMPLOYEE_MANAGEMENT', 'EXPORT', 'Export Employees', 'Download employee rosters'),
('USER_MANAGEMENT', 'VIEW', 'View Users', 'Inspect user credentials and status'),
('USER_MANAGEMENT', 'CREATE', 'Create User', 'Provision new user account'),
('USER_MANAGEMENT', 'UPDATE', 'Update User', 'Modify user details'),
('USER_MANAGEMENT', 'DELETE', 'Delete User', 'Deactivate or remove user'),
('BRANCH_MANAGEMENT', 'VIEW', 'View Branches', 'View branch list'),
('BRANCH_MANAGEMENT', 'CREATE', 'Create Branch', 'Add branch'),
('BRANCH_MANAGEMENT', 'UPDATE', 'Update Branch', 'Modify branch'),
('BRANCH_MANAGEMENT', 'DELETE', 'Delete Branch', 'Remove branch'),
('DEPARTMENT_MANAGEMENT', 'VIEW', 'View Departments', 'View departments'),
('DEPARTMENT_MANAGEMENT', 'CREATE', 'Create Department', 'Add department'),
('STORE_MANAGEMENT', 'VIEW', 'View Stores', 'View stores'),
('STORE_MANAGEMENT', 'CREATE', 'Create Store', 'Add store'),
('REPORTS', 'VIEW', 'View Reports', 'Inspect reports'),
('REPORTS', 'EXPORT', 'Export Reports', 'Export reports to CSV/PDF'),
('SETTINGS', 'VIEW', 'View Settings', 'View configuration'),
('SETTINGS', 'UPDATE', 'Update Settings', 'Change configuration'),
('DYNAMIC_PAGES', 'VIEW', 'View Dynamic Pages', 'Access dynamic form builder')
ON CONFLICT (page_id, action_code) DO NOTHING;

-- Super Admin Initial Organization Permissions for ORG001 (Approved Pages)
INSERT INTO org_page_permissions (organization_id, page_id, allowed_actions, status, granted_by) VALUES
('ORG001', 'DASHBOARD', 'VIEW', 'ACTIVE', 'SA001'),
('ORG001', 'EMPLOYEE_MANAGEMENT', 'VIEW,CREATE,UPDATE,DELETE,EXPORT', 'ACTIVE', 'SA001'),
('ORG001', 'USER_MANAGEMENT', 'VIEW,CREATE,UPDATE,DELETE', 'ACTIVE', 'SA001'),
('ORG001', 'BRANCH_MANAGEMENT', 'VIEW,CREATE,UPDATE,DELETE', 'ACTIVE', 'SA001'),
('ORG001', 'DEPARTMENT_MANAGEMENT', 'VIEW,CREATE', 'ACTIVE', 'SA001'),
('ORG001', 'STORE_MANAGEMENT', 'VIEW,CREATE', 'ACTIVE', 'SA001'),
('ORG001', 'REPORTS', 'VIEW,EXPORT', 'ACTIVE', 'SA001'),
('ORG001', 'DYNAMIC_PAGES', 'VIEW', 'ACTIVE', 'SA001')
ON CONFLICT (organization_id, page_id) DO NOTHING;

-- Permission Groups for ORG001
INSERT INTO permission_groups (id, organization_id, group_name, description, status) VALUES
('GRP001', 'ORG001', 'Org Administrator Group', 'Full administrative authority within ORG001', 'ACTIVE'),
('GRP002', 'ORG001', 'General Staff', 'Standard employee view operations', 'ACTIVE'),
('GRP003', 'ORG001', 'Auditors', 'Read and export access only', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- Group Permissions (Page Actions assigned to Groups)
INSERT INTO group_permissions (group_id, organization_id, page_id, permitted_actions) VALUES
('GRP001', 'ORG001', 'DASHBOARD', 'VIEW'),
('GRP001', 'ORG001', 'EMPLOYEE_MANAGEMENT', 'VIEW,CREATE,UPDATE,DELETE,EXPORT'),
('GRP001', 'ORG001', 'USER_MANAGEMENT', 'VIEW,CREATE,UPDATE,DELETE'),
('GRP001', 'ORG001', 'BRANCH_MANAGEMENT', 'VIEW,CREATE,UPDATE,DELETE'),
('GRP001', 'ORG001', 'DEPARTMENT_MANAGEMENT', 'VIEW,CREATE'),
('GRP001', 'ORG001', 'STORE_MANAGEMENT', 'VIEW,CREATE'),
('GRP001', 'ORG001', 'REPORTS', 'VIEW,EXPORT'),
('GRP001', 'ORG001', 'DYNAMIC_PAGES', 'VIEW'),
('GRP002', 'ORG001', 'DASHBOARD', 'VIEW'),
('GRP002', 'ORG001', 'EMPLOYEE_MANAGEMENT', 'VIEW'),
('GRP003', 'ORG001', 'DASHBOARD', 'VIEW'),
('GRP003', 'ORG001', 'REPORTS', 'VIEW,EXPORT')
ON CONFLICT (group_id, page_id) DO NOTHING;

-- User Assignments for default user US001
INSERT INTO user_group_assignments (user_id, organization_id, group_id) VALUES
('US001', 'ORG001', 'GRP001')
ON CONFLICT (user_id, group_id) DO NOTHING;

INSERT INTO user_entity_assignments (user_id, organization_id, entity_id) VALUES
('US001', 'ORG001', 'BR001'),
('US001', 'ORG001', 'DEPT001')
ON CONFLICT (user_id, entity_id) DO NOTHING;
