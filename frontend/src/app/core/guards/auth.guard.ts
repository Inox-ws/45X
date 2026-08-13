import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/role.model';

/** Redirects to /login when signed out (Section 12 - deny-by-default). */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  if (authService.isAuthenticated()) {
    return true;
  }
  return inject(Router).createUrlTree(['/login']);
};

/** Blocks the route unless the current user has at least one of the given roles (Section 4). */
export function roleGuard(allowedRoles: Role[]): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    if (authService.hasAnyRole(allowedRoles)) {
      return true;
    }
    return inject(Router).createUrlTree(['/dashboard']);
  };
}
