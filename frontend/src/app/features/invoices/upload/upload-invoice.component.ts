import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { provideNativeDateAdapter } from '@angular/material/core';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { validateFile } from '../../../shared/utils/file-validation';
import { InvoiceService } from '../../../core/services/invoice.service';
import { CustomerSummary, ExtractedInvoiceData, InvoiceResponse } from '../../../core/models/invoice.model';

const ALLOWED_EXTENSIONS = ['.pdf', '.xlsx'];
const MAX_SIZE_MB = 25;

type Stage = 'select' | 'uploading' | 'review' | 'saving' | 'done';

@Component({
  selector: 'app-upload-invoice',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatDatepickerModule,
    PageHeaderComponent
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './upload-invoice.component.html',
  styleUrl: './upload-invoice.component.scss'
})
export class UploadInvoiceComponent {
  readonly stage = signal<Stage>('select');
  readonly fileError = signal<string | null>(null);
  readonly submitError = signal<string | null>(null);
  readonly selectedFileName = signal<string | null>(null);
  readonly extracted = signal<ExtractedInvoiceData | null>(null);
  readonly savedInvoice = signal<InvoiceResponse | null>(null);
  readonly customers = signal<CustomerSummary[]>([]);

  private documentId: number | null = null;
  private selectedFile: File | null = null;

  readonly form;

  constructor(
    private readonly fb: FormBuilder,
    private readonly invoiceService: InvoiceService
  ) {
    this.form = this.fb.group({
      invoiceNumber: this.fb.control('', [Validators.required]),
      invoiceDate: this.fb.control<Date | null>(null, [Validators.required]),
      customerId: this.fb.control<number | null>(null, [Validators.required]),
      amount: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
      currency: this.fb.control('USD', [Validators.required])
    });

    this.invoiceService.listCustomers().subscribe({
      next: customers => this.customers.set(customers),
      error: () => this.customers.set([])
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.fileError.set(null);
    this.selectedFile = null;
    this.selectedFileName.set(null);

    if (!file) {
      return;
    }
    const error = validateFile(file, ALLOWED_EXTENSIONS, MAX_SIZE_MB);
    if (error) {
      this.fileError.set(error);
      return;
    }
    this.selectedFile = file;
    this.selectedFileName.set(file.name);
  }

  async uploadAndExtract(): Promise<void> {
    if (!this.selectedFile) {
      return;
    }
    this.stage.set('uploading');
    this.submitError.set(null);

    try {
      const response = await firstValueFrom(this.invoiceService.uploadInvoice(this.selectedFile));
      this.documentId = response.documentId;
      this.extracted.set(response.extracted);
      this.form.patchValue({
        invoiceNumber: response.extracted.invoiceNumber ?? '',
        invoiceDate: response.extracted.invoiceDate ? new Date(response.extracted.invoiceDate) : null,
        amount: response.extracted.amount,
        currency: response.extracted.currency ?? 'USD'
      });
      this.stage.set('review');
    } catch {
      this.submitError.set('Upload failed. Check the file and that the 45X Portal API is reachable, then try again.');
      this.stage.set('select');
    }
  }

  async save(): Promise<void> {
    if (this.form.invalid || this.documentId === null) {
      this.form.markAllAsTouched();
      return;
    }
    this.stage.set('saving');
    this.submitError.set(null);

    const value = this.form.getRawValue();
    try {
      const invoice = await firstValueFrom(this.invoiceService.confirmInvoice(this.documentId, {
        invoiceNumber: value.invoiceNumber!,
        invoiceDate: this.toIsoDate(value.invoiceDate!),
        customerId: value.customerId!,
        amount: value.amount!,
        currency: value.currency!
      }));
      this.savedInvoice.set(invoice);
      this.stage.set('done');
    } catch (error: any) {
      this.submitError.set(error?.error?.detail ?? 'Could not save the invoice. Please review the fields and try again.');
      this.stage.set('review');
    }
  }

  startOver(): void {
    this.stage.set('select');
    this.documentId = null;
    this.selectedFile = null;
    this.selectedFileName.set(null);
    this.extracted.set(null);
    this.savedInvoice.set(null);
    this.submitError.set(null);
    this.form.reset({ currency: 'USD' });
  }

  private toIsoDate(date: Date): string {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }
}
