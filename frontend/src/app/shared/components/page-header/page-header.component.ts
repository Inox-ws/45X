import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-page-header',
  standalone: true,
  template: `
    <div class="page-header">
      <h1>{{ title }}</h1>
      @if (subtitle) {
        <p class="page-header__subtitle">{{ subtitle }}</p>
      }
    </div>
  `,
  styles: [`
    .page-header { margin-bottom: 1.25rem; }
    .page-header h1 { margin: 0 0 0.25rem; font-size: 1.4rem; font-weight: 600; color: #1b3a2f; }
    .page-header__subtitle { margin: 0; color: #5f6b66; font-size: 0.9rem; }
  `]
})
export class PageHeaderComponent {
  @Input({ required: true }) title!: string;
  @Input() subtitle?: string;
}
