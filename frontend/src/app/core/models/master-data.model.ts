export interface SupplierResponse {
  id: number;
  name: string;
  countryOfOrigin: string;
  feocStatus: string;
  feocNotes: string | null;
  materialInfo: string | null;
  active: boolean;
}
export type SupplierRequest = Omit<SupplierResponse, 'id'>;

export interface CustomerFullResponse {
  id: number;
  name: string;
  address: string | null;
  contactName: string | null;
  contactEmail: string | null;
  contactPhone: string | null;
  active: boolean;
}
export type CustomerFullRequest = Omit<CustomerFullResponse, 'id'>;

export interface CreditRateResponse {
  id: number;
  componentType: string;
  ratePerWatt: number;
  effectiveFrom: string;
  effectiveTo: string | null;
}
export type CreditRateRequest = Omit<CreditRateResponse, 'id'>;

export interface FeocListEntryResponse {
  id: number;
  entryType: string;
  name: string;
  status: string;
  notes: string | null;
  effectiveFrom: string | null;
}
export type FeocListEntryRequest = Omit<FeocListEntryResponse, 'id'>;

export interface MaterialMasterResponse {
  id: number;
  materialCode: string;
  description: string | null;
  uom: string | null;
  source: string;
  lastSyncedAt: string | null;
}
export type MaterialMasterRequest = Omit<MaterialMasterResponse, 'id' | 'source' | 'lastSyncedAt'>;
