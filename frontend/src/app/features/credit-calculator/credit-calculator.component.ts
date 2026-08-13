import { Component } from '@angular/core';
import { ComingSoonComponent } from '../../shared/components/coming-soon/coming-soon.component';

@Component({
  selector: 'app-credit-calculator',
  standalone: true,
  imports: [ComingSoonComponent],
  template: `<app-coming-soon title="45X Credit Calculator" milestone="Milestone 6 (Engines)"></app-coming-soon>`
})
export class CreditCalculatorComponent {}
