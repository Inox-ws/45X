import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { UserService } from '../../../core/services/user.service';
import { AppUserResponse, RoleResponse } from '../../../core/models/user.model';

@Component({
  selector: 'app-manage-users',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatChipsModule,
    MatIconModule,
    MatProgressSpinnerModule,
    PageHeaderComponent
  ],
  templateUrl: './manage-users.component.html',
  styleUrl: './manage-users.component.scss'
})
export class ManageUsersComponent {
  readonly displayedColumns = ['fullName', 'email', 'roles', 'active', 'actions'];
  readonly users = signal<AppUserResponse[]>([]);
  readonly roles = signal<RoleResponse[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly editingUserId = signal<number | null>(null);

  readonly createForm;
  readonly rolesForm;

  constructor(
    private readonly fb: FormBuilder,
    private readonly userService: UserService
  ) {
    this.createForm = this.fb.group({
      fullName: this.fb.control('', [Validators.required]),
      email: this.fb.control('', [Validators.required, Validators.email]),
      password: this.fb.control(''),
      roles: this.fb.control<string[]>([], [Validators.required])
    });
    this.rolesForm = this.fb.group({
      roles: this.fb.control<string[]>([], [Validators.required])
    });

    this.userService.listRoles().subscribe({
      next: roles => this.roles.set(roles),
      error: () => this.roles.set([])
    });
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.userService.list().subscribe({
      next: users => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not load users. Is the 45X Portal API reachable?');
        this.loading.set(false);
      }
    });
  }

  async createUser(): Promise<void> {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }
    const value = this.createForm.getRawValue();
    try {
      await firstValueFrom(this.userService.create({
        fullName: value.fullName!,
        email: value.email!,
        password: value.password || undefined,
        roles: value.roles!
      }));
      this.createForm.reset({ roles: [] });
      this.load();
    } catch (error: any) {
      this.errorMessage.set(error?.error?.detail ?? 'Could not create user.');
    }
  }

  startEditRoles(user: AppUserResponse): void {
    this.editingUserId.set(user.id);
    this.rolesForm.setValue({ roles: user.roles });
  }

  async saveRoles(userId: number): Promise<void> {
    if (this.rolesForm.invalid) {
      return;
    }
    try {
      await firstValueFrom(this.userService.updateRoles(userId, this.rolesForm.getRawValue().roles!));
      this.editingUserId.set(null);
      this.load();
    } catch (error: any) {
      this.errorMessage.set(error?.error?.detail ?? 'Could not update roles.');
    }
  }

  async toggleActive(user: AppUserResponse): Promise<void> {
    try {
      await firstValueFrom(this.userService.setActive(user.id, !user.active));
      this.load();
    } catch {
      this.errorMessage.set('Could not update user status.');
    }
  }
}
