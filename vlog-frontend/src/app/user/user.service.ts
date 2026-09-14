import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE } from '../core/api.config';
import { CreateUserRequest, UserResponse } from './user.model';

@Injectable({ providedIn: 'root' })
export class UserService {

  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE}/users`;

  findAll(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(this.url);
  }

  findByUsername(username: string): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.url}/by-username/${username}`);
  }

  create(request: CreateUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(this.url, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
