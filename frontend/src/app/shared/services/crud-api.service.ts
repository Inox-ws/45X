import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { PageResponse } from '../../core/models/page-response.model';

/**
 * A small generic REST client reused across the Master Data screens (Section 5) -
 * Supplier/Customer/CreditRate/FeocListEntry/MaterialMaster all follow the
 * same list/get/create/update(/delete) shape, just with different DTOs and
 * base paths. Not @Injectable - each screen constructs its own instance
 * (`new CrudApiService(http, '/master-data/suppliers')`) since the base path
 * varies per entity.
 *
 * Some backend list endpoints return the Section 13 { items, page, size,
 * total } envelope and some return a plain array (see each controller) -
 * `paginated` tells this client which to expect; either way, list() always
 * resolves to a plain array for the caller.
 */
export class CrudApiService<TResponse, TRequest> {
  constructor(
    private readonly http: HttpClient,
    private readonly basePath: string,
    private readonly paginated: boolean = true
  ) {}

  list(): Observable<TResponse[]> {
    if (this.paginated) {
      return this.http.get<PageResponse<TResponse>>(this.basePath, { params: { size: 200 } })
        .pipe(map(response => response.items));
    }
    return this.http.get<TResponse[]>(this.basePath);
  }

  create(payload: TRequest): Observable<TResponse> {
    return this.http.post<TResponse>(this.basePath, payload);
  }

  update(id: number, payload: TRequest): Observable<TResponse> {
    return this.http.put<TResponse>(`${this.basePath}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.basePath}/${id}`);
  }
}
