import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PodUploadResponse } from '../models/pod.model';

@Injectable({ providedIn: 'root' })
export class PodService {
  constructor(private readonly http: HttpClient) {}

  uploadPod(file: File, invoiceId: number): Observable<PodUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('invoiceId', String(invoiceId));
    return this.http.post<PodUploadResponse>(`${environment.apiBaseUrl}/pod/upload`, formData);
  }
}
