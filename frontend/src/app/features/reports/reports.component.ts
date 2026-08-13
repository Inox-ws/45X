import { Component } from '@angular/core';
import { ComingSoonComponent } from '../../shared/components/coming-soon/coming-soon.component';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [ComingSoonComponent],
  template: `<app-coming-soon title="Reports" milestone="Milestone 7 (Dashboards & Reports)"></app-coming-soon>`
})
export class ReportsComponent {}
