import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Role } from '../models/role.model';
import { isEntraConfigured } from './msal-config';

export interface CurrentUser {
  email: string;
  displayName: string;
  roles: Role[];
}

const TOKEN_STORAGE_KEY = 'x45.localAuth.token';

interface LoginResponse {
  accessToken: string;
  expiresInMinutes: number;
  displayName: string;
  email: string;
  roles: string[];
}

/**
 * Abstracts over the two auth modes from Section 3: real Entra ID (via MSAL,
 * once environment.entra.clientId is configured) and the local-dev JWT
 * fallback (used whenever it isn't). Everything else in the app - guards,
 * the HTTP interceptor, the shell's user menu - goes through this service
 * rather than knowing which mode is active.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly _currentUser = signal<CurrentUser | null>(null);
  readonly currentUser = this._currentUser.asReadonly();

  constructor(private readonly http: HttpClient, private readonly router: Router) {
    if (!isEntraConfigured()) {
      this.restoreLocalSession();
    }
  }

  isAuthenticated(): boolean {
    return this._currentUser() !== null;
  }

  hasAnyRole(roles: Role[]): boolean {
    const user = this._currentUser();
    if (!user) {
      return false;
    }
    return roles.some(role => user.roles.includes(role));
  }

  /** Local-dev fallback login (Section 3). No-op / unused once Entra ID is configured. */
  async loginLocal(email: string, password: string): Promise<void> {
    const response = await firstValueFrom(
      this.http.post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, { email, password })
    );
    sessionStorage.setItem(TOKEN_STORAGE_KEY, response.accessToken);
    this._currentUser.set({
      email: response.email,
      displayName: response.displayName,
      roles: response.roles as Role[]
    });
  }

  logout(): void {
    sessionStorage.removeItem(TOKEN_STORAGE_KEY);
    this._currentUser.set(null);
    this.router.navigate(['/login']);
  }

  /** Bearer token for the auth interceptor to attach, or null if signed out. Entra ID token acquisition goes through MsalInterceptor instead, not this. */
  getLocalToken(): string | null {
    return sessionStorage.getItem(TOKEN_STORAGE_KEY);
  }

  private restoreLocalSession(): void {
    const token = this.getLocalToken();
    if (!token) {
      return;
    }
    // Re-fetch the profile rather than trusting decoded-but-unverified JWT payload client-side.
    this.http.get<{ email: string; displayName: string; roles: string[] }>(`${environment.apiBaseUrl}/auth/me`)
      .subscribe({
        next: profile => this._currentUser.set({ ...profile, roles: profile.roles as Role[] }),
        error: () => {
          sessionStorage.removeItem(TOKEN_STORAGE_KEY);
          this._currentUser.set(null);
        }
      });
  }
}
