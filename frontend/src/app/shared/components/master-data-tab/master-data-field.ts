export interface MasterDataFieldOption {
  value: string;
  label: string;
}

export interface MasterDataField {
  key: string;
  label: string;
  type: 'text' | 'number' | 'date' | 'select' | 'checkbox' | 'textarea';
  options?: MasterDataFieldOption[];
  required?: boolean;
  /** Hide from the table (still editable in the form) - e.g. long notes fields. */
  hiddenInTable?: boolean;
}
