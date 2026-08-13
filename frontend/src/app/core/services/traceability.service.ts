import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TraceabilityAnchor, TraceabilityChain } from '../models/traceability.model';

@Injectable({ providedIn: 'root' })
export class TraceabilityService {
  constructor(private readonly http: HttpClient) {}

  lookup(anchor: TraceabilityAnchor, value: string): Observable<TraceabilityChain> {
    return this.http.get<TraceabilityChain>(`${environment.apiBaseUrl}/traceability/${anchor}/${encodeURIComponent(value)}`);
  }
}
