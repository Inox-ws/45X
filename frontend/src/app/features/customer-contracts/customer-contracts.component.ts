import { Component } from '@angular/core';
import { ComingSoonComponent } from '../../shared/components/coming-soon/coming-soon.component';

@Component({
  selector: 'app-customer-contracts',
  standalone: true,
  imports: [ComingSoonComponent],
  template: `<app-coming-soon title="Customer Contracts" milestone="Milestone 5 (repository-style screens, matching reference portal)"></app-coming-soon>`
})
export class CustomerContractsComponent {}
