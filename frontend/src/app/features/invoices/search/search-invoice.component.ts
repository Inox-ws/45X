import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { provideNativeDateAdapter } from '@angular/material/core';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { InvoiceService } from '../../../core/services/invoice.service';
import { DocumentService } from '../../../core/services/document.service';
import { CustomerSummary, InvoiceDetail, InvoiceListItem } from '../../../core/models/invoice.model';

const STATUSES = ['DRAFT', 'PENDING_VALIDATION', 'VALIDATED', 'PAID', 'DISPUTED'];

@Component({
  selector: 'app-search-invoice',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatTableModule,
    MatPaginatorModule,
    MatCardModule,
    MatIconModule,
    MatDatepickerModule,
    MatProgressSpinnerModule,
    PageHeaderComponent
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './search-invoice.component.html',
  styleUrl: './search-invoice.component.scss'
})
export class SearchInvoiceComponent {
  readonly displayedColumns = ['invoiceNumber', 'customerName', 'invoiceDate', 'amount', 'status', 'actions'];
  readonly statuses = STATUSES;

  readonly results = signal<InvoiceListItem[]>([]);
  readonly total = signal(0);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly customers = signal<CustomerSummary[]>([]);
  readonly selectedDetail = signal<InvoiceDetail | null>(null);

  private pageIndex = 0;
  private pageSize = 20;

  readonly form;

  constructor(
    private readonly fb: FormBuilder,
    private readonly invoiceService: InvoiceService,
    private readonly documentService: DocumentService
  ) {
    this.form = this.fb.group({
      invoiceNumber: this.fb.control(''),
      customerId: this.fb.control<number | null>(null),
      status: this.fb.control<string | null>(null),
      dateFrom: this.fb.control<Date | null>(null),
      dateTo: this.fb.control<Date | null>(null)
    });

    this.invoiceService.listCustomers().subscribe({
      next: customers => this.customers.set(customers),
      error: () => this.customers.set([])
    });

    this.search();
  }

  search(): void {
    this.pageIndex = 0;
    this.runSearch();
  }

  onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.runSearch();
  }

  private runSearch(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();

    this.invoiceService.search({
      invoiceNumber: value.invoiceNumber || undefined,
      customerId: value.customerId ?? undefined,
      status: value.status ?? undefined,
      dateFrom: value.dateFrom ? this.toIsoDate(value.dateFrom) : undefined,
      dateTo: value.dateTo ? this.toIsoDate(value.dateTo) : undefined,
      page: this.pageIndex,
      size: this.pageSize
    }).subscribe({
      next: response => {
        this.results.set(response.items);
        this.total.set(response.total);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not search invoices. Is the 45X Portal API reachable?');
        this.loading.set(false);
      }
    });
  }

  async viewDetail(id: number): Promise<void> {
    try {
      const detail = await firstValueFrom(this.invoiceService.getDetail(id));
      this.selectedDetail.set(detail);
    } catch {
      this.errorMessage.set('Could not load invoice detail.');
    }
  }

  closeDetail(): void {
    this.selectedDetail.set(null);
  }

  downloadDocument(documentId: number, fileName: string): void {
    this.documentService.download(documentId, fileName).catch(() => this.errorMessage.set('Download failed.'));
  }

  async exportCsv(): Promise<void> {
    const value = this.form.getRawValue();
    try {
      const blob = await firstValueFrom(this.invoiceService.export({
        invoiceNumber: value.invoiceNumber || undefined,
        customerId: value.customerId ?? undefined,
        status: value.status ?? undefined,
        dateFrom: value.dateFrom ? this.toIsoDate(value.dateFrom) : undefined,
        dateTo: value.dateTo ? this.toIsoDate(value.dateTo) : undefined
      }));
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = 'invoices.csv';
      anchor.click();
      URL.revokeObjectURL(url);
    } catch {
      this.errorMessage.set('Export failed.');
    }
  }

  private toIsoDate(date: Date): string {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }
}
