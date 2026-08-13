export interface ExtractedLineItem {
  description: string | null;
  quantity: number | null;
  unitPrice: number | null;
  amount: number | null;
  wattage: number | null;
}

export interface ExtractedInvoiceData {
  invoiceNumber: string | null;
  invoiceDate: string | null; // ISO date
  customerName: string | null;
  amount: number | null;
  currency: string | null;
  lineItems: ExtractedLineItem[];
  totalWattage: number | null;
}

export interface UploadInvoiceResponse {
  documentId: number;
  fileName: string;
  extracted: ExtractedInvoiceData;
}

export interface ConfirmInvoiceRequest {
  invoiceNumber: string;
  invoiceDate: string; // ISO date
  customerId: number;
  amount: number;
  currency: string;
}

export interface InvoiceResponse {
  id: number;
  invoiceNumber: string;
  customerId: number;
  customerName: string;
  invoiceDate: string;
  amount: number;
  currency: string;
  status: string;
  documentId: number;
}

export interface CustomerSummary {
  id: number;
  name: string;
}

export interface InvoiceListItem {
  id: number;
  invoiceNumber: string;
  customerName: string;
  invoiceDate: string;
  amount: number;
  currency: string;
  status: string;
}

export interface DocumentSummary {
  id: number;
  documentType: string;
  fileName: string;
  uploadedAt: string;
}

export interface InvoiceDetail {
  id: number;
  invoiceNumber: string;
  customerId: number;
  customerName: string;
  invoiceDate: string;
  amount: number;
  currency: string;
  status: string;
  source: string;
  documents: DocumentSummary[];
}

export interface InvoiceSearchFilters {
  invoiceNumber?: string;
  customerId?: number;
  supplierId?: number;
  status?: string;
  dateFrom?: string;
  dateTo?: string;
  page?: number;
  size?: number;
}
