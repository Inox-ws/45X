import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';

interface KpiCard {
  label: string;
  value: string;
  icon: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [MatCardModule, MatIconModule, PageHeaderComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  // Milestone 1 placeholder values. Real KPIs (Section 10) are wired to
  // report APIs in Milestone 7.
  readonly kpis: KpiCard[] = [
    { label: 'Credit YTD', value: '—', icon: 'calculate' },
    { label: 'Eligible Wattage (Month)', value: '—', icon: 'bolt' },
    { label: 'FEOC Compliance', value: '—', icon: 'verified_user' },
    { label: 'Open Invoices', value: '—', icon: 'receipt_long' }
  ];
}
