import { Component } from '@angular/core';
import { ComingSoonComponent } from '../../../shared/components/coming-soon/coming-soon.component';

@Component({
  selector: 'app-activity-log',
  standalone: true,
  imports: [ComingSoonComponent],
  template: `<app-coming-soon title="Activity Log" milestone="Milestone 6 (Audit trail)"></app-coming-soon>`
})
export class ActivityLogComponent {}
