import { Role } from '../post/post.model';

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  role: Role;
  createdAt: string;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  role?: Role;
}
