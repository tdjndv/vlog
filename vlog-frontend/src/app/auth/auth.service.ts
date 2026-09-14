import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { API_BASE } from '../core/api.config';
import { AuthResponse, LoginRequest, SignupRequest } from './auth.model';
import { Role } from '../post/post.model';

const TOKEN_KEY = 'vlog.token';
const USER_KEY = 'vlog.user';

interface StoredUser {
  username: string;
  role: Role;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly url = `${API_BASE}/auth`;

  private readonly currentUser = signal<StoredUser | null>(this.readStoredUser());

  readonly user = this.currentUser.asReadonly();
  readonly isLoggedIn = computed(() => this.currentUser() !== null);
  readonly isManager = computed(() => this.currentUser()?.role === 'MANAGER');
  readonly username = computed(() => this.currentUser()?.username ?? null);

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.url}/login`, request)
      .pipe(tap(response => this.store(response)));
  }

  signup(request: SignupRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.url}/signup`, request)
      .pipe(tap(response => this.store(response)));
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  /** True if the logged-in user may modify a post by this author. */
  canModify(authorUsername: string): boolean {
    const user = this.currentUser();
    if (!user) return false;
    return user.username === authorUsername || user.role === 'MANAGER';
  }

  private store(response: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify({
      username: response.username,
      role: response.role
    }));
    this.currentUser.set({ username: response.username, role: response.role });
  }

  private readStoredUser(): StoredUser | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as StoredUser;
    } catch {
      return null;
    }
  }
}
