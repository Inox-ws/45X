import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { TraceabilityService } from '../../core/services/traceability.service';
import { TraceabilityAnchor, TraceabilityChain } from '../../core/models/traceability.model';

@Component({
  selector: 'app-traceability',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    PageHeaderComponent
  ],
  templateUrl: './traceability.component.html',
  styleUrl: './traceability.component.scss'
})
export class TraceabilityComponent {
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly chain = signal<TraceabilityChain | null>(null);

  readonly form;

  constructor(
    private readonly fb: FormBuilder,
    private readonly traceabilityService: TraceabilityService
  ) {
    this.form = this.fb.group({
      anchor: this.fb.control<TraceabilityAnchor>('cells', [Validators.required]),
      value: this.fb.control('', [Validators.required])
    });
  }

  async search(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.errorMessage.set(null);
    this.chain.set(null);
    const { anchor, value } = this.form.getRawValue();

    try {
      const chain = await firstValueFrom(this.traceabilityService.lookup(anchor!, value!.trim()));
      this.chain.set(chain);
      if (chain.modules.length === 0 && chain.invoices.length === 0) {
        this.errorMessage.set('No traceability chain found - this cell/module may not be linked into a module or invoice yet.');
      }
    } catch {
      this.errorMessage.set('Not found, or the 45X Portal API is unreachable.');
    } finally {
      this.loading.set(false);
    }
  }
}
