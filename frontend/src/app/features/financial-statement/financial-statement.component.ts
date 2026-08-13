import { Component } from '@angular/core';
import { ComingSoonComponent } from '../../shared/components/coming-soon/coming-soon.component';

@Component({
  selector: 'app-financial-statement',
  standalone: true,
  imports: [ComingSoonComponent],
  template: `<app-coming-soon title="Financial Statement" milestone="Milestone 5 (repository-style screens, matching reference portal)"></app-coming-soon>`
})
export class FinancialStatementComponent {}
