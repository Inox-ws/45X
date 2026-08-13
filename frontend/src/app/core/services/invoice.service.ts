import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ConfirmInvoiceRequest, CustomerSummary, InvoiceDetail, InvoiceListItem, InvoiceResponse,
  InvoiceSearchFilters, UploadInvoiceResponse
} from '../models/invoice.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class InvoiceService {
  constructor(private readonly http: HttpClient) {}

  uploadInvoice(file: File): Observable<UploadInvoiceResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<UploadInvoiceResponse>(`${environment.apiBaseUrl}/invoices/upload`, formData);
  }

  confirmInvoice(documentId: number, request: ConfirmInvoiceRequest): Observable<InvoiceResponse> {
    return this.http.post<InvoiceResponse>(`${environment.apiBaseUrl}/invoices/${documentId}/confirm`, request);
  }

  listCustomers(): Observable<CustomerSummary[]> {
    return this.http.get<CustomerSummary[]>(`${environment.apiBaseUrl}/customers`);
  }

  search(filters: InvoiceSearchFilters): Observable<PageResponse<InvoiceListItem>> {
    let params: Record<string, string> = {};
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params[key] = String(value);
      }
    });
    return this.http.get<PageResponse<InvoiceListItem>>(`${environment.apiBaseUrl}/invoices`, { params });
  }

  getDetail(id: number): Observable<InvoiceDetail> {
    return this.http.get<InvoiceDetail>(`${environment.apiBaseUrl}/invoices/${id}`);
  }

  export(filters: InvoiceSearchFilters): Observable<Blob> {
    let params: Record<string, string> = {};
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '' && key !== 'page' && key !== 'size') {
        params[key] = String(value);
      }
    });
    return this.http.get(`${environment.apiBaseUrl}/invoices/export`, { params, responseType: 'blob' });
  }
}
