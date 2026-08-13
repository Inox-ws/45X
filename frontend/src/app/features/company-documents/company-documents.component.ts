import { Component } from '@angular/core';
import { ComingSoonComponent } from '../../shared/components/coming-soon/coming-soon.component';

@Component({
  selector: 'app-company-documents',
  standalone: true,
  imports: [ComingSoonComponent],
  template: `<app-coming-soon title="Company Documents" milestone="Milestone 5 (repository-style screens, matching reference portal)"></app-coming-soon>`
})
export class CompanyDocumentsComponent {}
