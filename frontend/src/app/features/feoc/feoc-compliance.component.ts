import { Component } from '@angular/core';
import { ComingSoonComponent } from '../../shared/components/coming-soon/coming-soon.component';

@Component({
  selector: 'app-feoc-compliance',
  standalone: true,
  imports: [ComingSoonComponent],
  template: `<app-coming-soon title="FEOC Compliance" milestone="Milestone 6 (Engines)"></app-coming-soon>`
})
export class FeocComplianceComponent {}
