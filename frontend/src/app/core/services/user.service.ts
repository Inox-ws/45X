import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AppUserResponse, CreateUserRequest, RoleResponse } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private readonly http: HttpClient) {}

  list(): Observable<AppUserResponse[]> {
    return this.http.get<AppUserResponse[]>(`${environment.apiBaseUrl}/users`);
  }

  create(request: CreateUserRequest): Observable<AppUserResponse> {
    return this.http.post<AppUserResponse>(`${environment.apiBaseUrl}/users`, request);
  }

  updateRoles(id: number, roles: string[]): Observable<AppUserResponse> {
    return this.http.put<AppUserResponse>(`${environment.apiBaseUrl}/users/${id}/roles`, { roles });
  }

  setActive(id: number, active: boolean): Observable<AppUserResponse> {
    return this.http.patch<AppUserResponse>(`${environment.apiBaseUrl}/users/${id}/active`, { active });
  }

  listRoles(): Observable<RoleResponse[]> {
    return this.http.get<RoleResponse[]>(`${environment.apiBaseUrl}/roles`);
  }
}
