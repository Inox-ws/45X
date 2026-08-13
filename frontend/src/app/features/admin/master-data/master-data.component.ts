import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatTabsModule } from '@angular/material/tabs';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { MasterDataTabComponent } from '../../../shared/components/master-data-tab/master-data-tab.component';
import { MasterDataField } from '../../../shared/components/master-data-tab/master-data-field';
import { CrudApiService } from '../../../shared/services/crud-api.service';
import { environment } from '../../../../environments/environment';
import {
  CreditRateRequest, CreditRateResponse, CustomerFullRequest, CustomerFullResponse,
  FeocListEntryRequest, FeocListEntryResponse, MaterialMasterRequest, MaterialMasterResponse,
  SupplierRequest, SupplierResponse
} from '../../../core/models/master-data.model';

const FEOC_STATUS_OPTIONS = [
  { value: 'PASS', label: 'Pass' },
  { value: 'FAIL', label: 'Fail' },
  { value: 'NEEDS_REVIEW', label: 'Needs review' }
];

const COMPONENT_TYPE_OPTIONS = [
  { value: 'SOLAR_CELL', label: 'Solar cell' },
  { value: 'SOLAR_MODULE', label: 'Solar module' }
];

const FEOC_LIST_STATUS_OPTIONS = [
  { value: 'PROHIBITED', label: 'Prohibited' },
  { value: 'RESTRICTED', label: 'Restricted' },
  { value: 'ALLOWED', label: 'Allowed' }
];

const FEOC_ENTRY_TYPE_OPTIONS = [
  { value: 'COUNTRY', label: 'Country' },
  { value: 'ENTITY', label: 'Entity' }
];

/** Master Data (Section 5): Suppliers, Customers, Credit Rates, FEOC list, Materials - each an app-master-data-tab. */
@Component({
  selector: 'app-master-data',
  standalone: true,
  imports: [MatTabsModule, PageHeaderComponent, MasterDataTabComponent],
  templateUrl: './master-data.component.html'
})
export class MasterDataComponent {
  readonly supplierService: CrudApiService<SupplierResponse, SupplierRequest>;
  readonly customerService: CrudApiService<CustomerFullResponse, CustomerFullRequest>;
  readonly creditRateService: CrudApiService<CreditRateResponse, CreditRateRequest>;
  readonly feocListService: CrudApiService<FeocListEntryResponse, FeocListEntryRequest>;
  readonly materialService: CrudApiService<MaterialMasterResponse, MaterialMasterRequest>;

  readonly supplierFields: MasterDataField[] = [
    { key: 'name', label: 'Name', type: 'text', required: true },
    { key: 'countryOfOrigin', label: 'Country of origin', type: 'text', required: true },
    { key: 'feocStatus', label: 'FEOC status', type: 'select', options: FEOC_STATUS_OPTIONS },
    { key: 'feocNotes', label: 'FEOC notes', type: 'textarea', hiddenInTable: true },
    { key: 'materialInfo', label: 'Material info', type: 'textarea', hiddenInTable: true },
    { key: 'active', label: 'Active', type: 'checkbox' }
  ];

  readonly customerFields: MasterDataField[] = [
    { key: 'name', label: 'Name', type: 'text', required: true },
    { key: 'address', label: 'Address', type: 'text' },
    { key: 'contactName', label: 'Contact name', type: 'text' },
    { key: 'contactEmail', label: 'Contact email', type: 'text' },
    { key: 'contactPhone', label: 'Contact phone', type: 'text', hiddenInTable: true },
    { key: 'active', label: 'Active', type: 'checkbox' }
  ];

  readonly creditRateFields: MasterDataField[] = [
    { key: 'componentType', label: 'Component type', type: 'select', options: COMPONENT_TYPE_OPTIONS, required: true },
    { key: 'ratePerWatt', label: 'Rate per watt ($)', type: 'number', required: true },
    { key: 'effectiveFrom', label: 'Effective from', type: 'date', required: true },
    { key: 'effectiveTo', label: 'Effective to', type: 'date' }
  ];

  readonly feocListFields: MasterDataField[] = [
    { key: 'entryType', label: 'Type', type: 'select', options: FEOC_ENTRY_TYPE_OPTIONS, required: true },
    { key: 'name', label: 'Country / entity name', type: 'text', required: true },
    { key: 'status', label: 'Status', type: 'select', options: FEOC_LIST_STATUS_OPTIONS, required: true },
    { key: 'notes', label: 'Notes', type: 'textarea', hiddenInTable: true },
    { key: 'effectiveFrom', label: 'Effective from', type: 'date' }
  ];

  readonly materialFields: MasterDataField[] = [
    { key: 'materialCode', label: 'Material code', type: 'text', required: true },
    { key: 'description', label: 'Description', type: 'text' },
    { key: 'uom', label: 'UoM', type: 'text' }
  ];

  constructor(http: HttpClient) {
    this.supplierService = new CrudApiService(http, `${environment.apiBaseUrl}/master-data/suppliers`, true);
    this.customerService = new CrudApiService(http, `${environment.apiBaseUrl}/master-data/customers`, true);
    this.creditRateService = new CrudApiService(http, `${environment.apiBaseUrl}/master-data/credit-rates`, false);
    this.feocListService = new CrudApiService(http, `${environment.apiBaseUrl}/master-data/feoc-list`, false);
    this.materialService = new CrudApiService(http, `${environment.apiBaseUrl}/master-data/materials`, true);
  }
}
