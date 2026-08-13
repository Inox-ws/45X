export interface AppUserResponse {
  id: number;
  fullName: string;
  email: string;
  active: boolean;
  roles: string[];
}

export interface CreateUserRequest {
  fullName: string;
  email: string;
  password?: string;
  roles: string[];
}

export interface RoleResponse {
  id: number;
  name: string;
  description: string | null;
}
