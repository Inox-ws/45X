import { Routes } from '@angular/router';
import { ShellComponent } from './shared/layout/shell/shell.component';
import { authGuard, roleGuard } from './core/guards/auth.guard';
import { Role } from './core/models/role.model';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'invoices',
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'search' },
          {
            path: 'upload',
            canActivate: [roleGuard([Role.Finance, Role.Admin])],
            loadComponent: () => import('./features/invoices/upload/upload-invoice.component').then(m => m.UploadInvoiceComponent)
          },
          {
            path: 'search',
            canActivate: [roleGuard([Role.Finance, Role.Management, Role.Admin])],
            loadComponent: () => import('./features/invoices/search/search-invoice.component').then(m => m.SearchInvoiceComponent)
          }
        ]
      },
      {
        path: 'pod',
        canActivate: [roleGuard([Role.Logistics, Role.Admin])],
        loadComponent: () => import('./features/pod/upload-pod.component').then(m => m.UploadPodComponent)
      },
      {
        path: 'traceability',
        canActivate: [roleGuard([Role.Production, Role.Management, Role.Admin])],
        loadComponent: () => import('./features/traceability/traceability.component').then(m => m.TraceabilityComponent)
      },
      {
        path: 'feoc',
        canActivate: [roleGuard([Role.Production, Role.Management, Role.Admin])],
        loadComponent: () => import('./features/feoc/feoc-compliance.component').then(m => m.FeocComplianceComponent)
      },
      {
        path: 'credit-calculator',
        canActivate: [roleGuard([Role.Finance, Role.Management, Role.Admin])],
        loadComponent: () => import('./features/credit-calculator/credit-calculator.component').then(m => m.CreditCalculatorComponent)
      },
      {
        path: 'reports',
        canActivate: [roleGuard([Role.Finance, Role.Production, Role.Management, Role.Admin])],
        loadComponent: () => import('./features/reports/reports.component').then(m => m.ReportsComponent)
      },
      {
        path: 'company-documents',
        canActivate: [roleGuard([Role.Finance, Role.Management, Role.Admin])],
        loadComponent: () => import('./features/company-documents/company-documents.component').then(m => m.CompanyDocumentsComponent)
      },
      {
        path: 'customer-contracts',
        canActivate: [roleGuard([Role.Finance, Role.Management, Role.Admin])],
        loadComponent: () => import('./features/customer-contracts/customer-contracts.component').then(m => m.CustomerContractsComponent)
      },
      {
        path: 'financial-statement',
        canActivate: [roleGuard([Role.Finance, Role.Management, Role.Admin])],
        loadComponent: () => import('./features/financial-statement/financial-statement.component').then(m => m.FinancialStatementComponent)
      },
      {
        path: 'admin',
        canActivate: [roleGuard([Role.Admin])],
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'users' },
          {
            path: 'users',
            loadComponent: () => import('./features/admin/users/manage-users.component').then(m => m.ManageUsersComponent)
          },
          {
            path: 'master-data',
            loadComponent: () => import('./features/admin/master-data/master-data.component').then(m => m.MasterDataComponent)
          },
          {
            path: 'activity-log',
            loadComponent: () => import('./features/admin/activity-log/activity-log.component').then(m => m.ActivityLogComponent)
          }
        ]
      },
      { path: '**', redirectTo: 'dashboard' }
    ]
  }
];
