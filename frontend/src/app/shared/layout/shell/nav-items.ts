import { Role } from '../../../core/models/role.model';

export interface NavItem {
  label: string;
  route: string;
  icon: string;
  roles: Role[];
  children?: NavItem[];
}

const ALL_ROLES = [Role.Finance, Role.Logistics, Role.Production, Role.Management, Role.Admin];

// Section 5 (functional modules) + Section 4 (RBAC) + the reference portal's
// document-repository style screens (Company Documents, Customer Contracts,
// Financial Statement).
export const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard', route: '/dashboard', icon: 'dashboard', roles: ALL_ROLES },
  {
    label: 'Invoices',
    route: '/invoices',
    icon: 'receipt_long',
    roles: [Role.Finance, Role.Management, Role.Admin],
    children: [
      { label: 'Upload Invoice', route: '/invoices/upload', icon: 'upload_file', roles: [Role.Finance, Role.Admin] },
      { label: 'Search Invoice', route: '/invoices/search', icon: 'search', roles: [Role.Finance, Role.Management, Role.Admin] }
    ]
  },
  { label: 'Upload POD', route: '/pod', icon: 'local_shipping', roles: [Role.Logistics, Role.Admin] },
  { label: 'Traceability', route: '/traceability', icon: 'account_tree', roles: [Role.Production, Role.Management, Role.Admin] },
  { label: 'FEOC Compliance', route: '/feoc', icon: 'verified_user', roles: [Role.Production, Role.Management, Role.Admin] },
  { label: '45X Credit Details', route: '/credit-calculator', icon: 'calculate', roles: [Role.Finance, Role.Management, Role.Admin] },
  { label: 'Reports', route: '/reports', icon: 'bar_chart', roles: ALL_ROLES.filter(r => r !== Role.Logistics) },
  { label: 'Company Documents', route: '/company-documents', icon: 'folder_shared', roles: [Role.Finance, Role.Management, Role.Admin] },
  { label: 'Customer Contracts', route: '/customer-contracts', icon: 'description', roles: [Role.Finance, Role.Management, Role.Admin] },
  { label: 'Financial Statement', route: '/financial-statement', icon: 'request_quote', roles: [Role.Finance, Role.Management, Role.Admin] },
  {
    label: 'Admin',
    route: '/admin',
    icon: 'admin_panel_settings',
    roles: [Role.Admin],
    children: [
      { label: 'Manage Users Role', route: '/admin/users', icon: 'group', roles: [Role.Admin] },
      { label: 'Master Data', route: '/admin/master-data', icon: 'inventory_2', roles: [Role.Admin] },
      { label: 'Activity Log', route: '/admin/activity-log', icon: 'history', roles: [Role.Admin, Role.Management] }
    ]
  }
];
