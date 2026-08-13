export interface SupplierSummary {
  id: number;
  name: string;
  countryOfOrigin: string;
  feocStatus: string;
}

export interface CellSummary {
  id: number;
  cellSerialNumber: string;
  batch: string | null;
  lot: string | null;
  wattage: number;
  countryOfOrigin: string;
  feocStatus: string;
  supplier: SupplierSummary;
}

export interface ModuleSummary {
  id: number;
  moduleSerialNumber: string;
  wattage: number;
  productionDate: string | null;
  cells: CellSummary[];
}

export interface TraceabilityInvoiceSummary {
  id: number;
  invoiceNumber: string;
  invoiceDate: string;
  amount: number;
  currency: string;
  status: string;
  customerName: string;
}

export interface TraceabilityChain {
  modules: ModuleSummary[];
  invoices: TraceabilityInvoiceSummary[];
}

export type TraceabilityAnchor = 'cells' | 'modules' | 'invoices';
