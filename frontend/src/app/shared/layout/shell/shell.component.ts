import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { NAV_ITEMS, NavItem } from './nav-items';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule
  ],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss'
})
export class ShellComponent {
  readonly user;

  constructor(private readonly authService: AuthService) {
    this.user = this.authService.currentUser;
  }

  readonly visibleNavItems = computed<NavItem[]>(() => {
    const roles = this.user()?.roles ?? [];
    const filterByRole = (item: NavItem): NavItem | null => {
      if (!item.roles.some(r => roles.includes(r))) {
        return null;
      }
      const children = item.children?.map(filterByRole).filter((c): c is NavItem => c !== null);
      return { ...item, children };
    };
    return NAV_ITEMS.map(filterByRole).filter((i): i is NavItem => i !== null);
  });

  signOut(): void {
    this.authService.logout();
  }
}
