import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { isEntraConfigured } from '../services/msal-config';
import { environment } from '../../../environments/environment';

/**
 * Attaches the local-dev fallback JWT to API requests (Section 3, Section 12).
 * When Entra ID is configured, MsalInterceptor (registered alongside this one
 * in app.config.ts) handles token attachment instead and this is a no-op.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (isEntraConfigured() || !req.url.startsWith(environment.apiBaseUrl)) {
    return next(req);
  }

  const token = inject(AuthService).getLocalToken();
  if (!token) {
    return next(req);
  }

  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
