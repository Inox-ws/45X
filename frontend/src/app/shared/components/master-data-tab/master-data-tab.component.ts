import { Component, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CrudApiService } from '../../services/crud-api.service';
import { MasterDataField } from './master-data-field';

/**
 * Generic list + create/edit form for a Master Data entity (Section 5) - one
 * component reused for Suppliers, Customers, Credit Rates, the FEOC list, and
 * Materials, each configured with its own field list and CrudApiService
 * instance rather than five near-identical hand-written screens.
 */
@Component({
  selector: 'app-master-data-tab',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './master-data-tab.component.html',
  styleUrl: './master-data-tab.component.scss'
})
export class MasterDataTabComponent<TResponse extends { id: number }, TRequest> implements OnInit {
  @Input({ required: true }) fields!: MasterDataField[];
  @Input({ required: true }) service!: CrudApiService<TResponse, TRequest>;
  @Input() supportsDelete = false;
  @Input() entityLabel = 'record';

  readonly rows = signal<TResponse[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly editingId = signal<number | null>(null);

  form!: FormGroup;

  constructor(private readonly fb: FormBuilder) {}

  ngOnInit(): void {
    const controls: Record<string, any> = {};
    for (const field of this.fields) {
      const defaultValue = field.type === 'checkbox' ? false : field.type === 'number' ? null : '';
      controls[field.key] = this.fb.control(defaultValue, field.required ? [Validators.required] : []);
    }
    this.form = this.fb.group(controls);
    this.load();
  }

  get tableColumns(): string[] {
    return [...this.fields.filter(f => !f.hiddenInTable).map(f => f.key), 'actions'];
  }

  load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.service.list().subscribe({
      next: rows => {
        this.rows.set(rows);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set(`Could not load ${this.entityLabel}s. Is the 45X Portal API reachable?`);
        this.loading.set(false);
      }
    });
  }

  startCreate(): void {
    this.editingId.set(null);
    this.form.reset();
  }

  startEdit(row: TResponse): void {
    this.editingId.set(row.id);
    this.form.patchValue(row as any);
  }

  async save(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.errorMessage.set(null);
    const payload = this.form.getRawValue() as TRequest;

    try {
      const id = this.editingId();
      if (id === null) {
        await firstValueFrom(this.service.create(payload));
      } else {
        await firstValueFrom(this.service.update(id, payload));
      }
      this.startCreate();
      this.load();
    } catch (error: any) {
      this.errorMessage.set(error?.error?.detail ?? `Could not save this ${this.entityLabel}.`);
    }
  }

  async remove(row: TResponse): Promise<void> {
    try {
      await firstValueFrom(this.service.delete(row.id));
      this.load();
    } catch {
      this.errorMessage.set(`Could not delete this ${this.entityLabel}.`);
    }
  }

  cellValue(row: TResponse, key: string): unknown {
    return (row as any)[key];
  }
}
