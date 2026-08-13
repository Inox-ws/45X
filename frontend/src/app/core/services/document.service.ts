import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DownloadUrlResponse } from '../models/document.model';

/**
 * Downloads a document via a short-lived SAS URL when the storage backend
 * supports one (azure profile), falling back to the authenticated /raw
 * endpoint otherwise (local profile) - see Section 9 and DocumentController.
 * Always fetches as a blob through HttpClient rather than a plain link click,
 * so the auth interceptor can attach a bearer token to same-origin requests
 * while correctly NOT doing so for a foreign SAS URL host.
 */
@Injectable({ providedIn: 'root' })
export class DocumentService {
  constructor(private readonly http: HttpClient) {}

  async download(documentId: number, fileName: string): Promise<void> {
    const { sasUrl } = await firstValueFrom(
      this.http.get<DownloadUrlResponse>(`${environment.apiBaseUrl}/documents/${documentId}/download-url`)
    );
    const url = sasUrl ?? `${environment.apiBaseUrl}/documents/${documentId}/raw`;
    const blob = await firstValueFrom(this.http.get(url, { responseType: 'blob' }));

    const objectUrl = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = objectUrl;
    anchor.download = fileName;
    anchor.click();
    URL.revokeObjectURL(objectUrl);
  }
}
