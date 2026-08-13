import { Component, Input } from '@angular/core';
import { PageHeaderComponent } from '../page-header/page-header.component';

@Component({
  selector: 'app-coming-soon',
  standalone: true,
  imports: [PageHeaderComponent],
  template: `
    <app-page-header [title]="title" [subtitle]="milestone ? 'Scaffolded in Milestone 1 - functionality lands in ' + milestone : undefined"></app-page-header>
    <p>This screen is routed and role-guarded; the working feature will be built in a later milestone per the build order in Section 18.</p>
  `
})
export class ComingSoonComponent {
  @Input({ required: true }) title!: string;
  @Input() milestone?: string;
}
