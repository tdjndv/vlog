import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE } from '../core/api.config';
import { CreatePostRequest, Page, PostFilter, PostResponse } from './post.model';

export interface PageRequest {
  page?: number;
  size?: number;
  sort?: string;          // e.g. 'postDate,desc'
}

@Injectable({ providedIn: 'root' })
export class PostService {

  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE}/posts`;

  search(filter: PostFilter = {}, page: PageRequest = {}): Observable<Page<PostResponse>> {
    let params = new HttpParams();

    for (const [key, value] of Object.entries({ ...filter, ...page })) {
      if (value !== null && value !== undefined && value !== '') {
        params = params.set(key, String(value));
      }
    }

    return this.http.get<Page<PostResponse>>(this.url, { params });
  }

  findById(id: number): Observable<PostResponse> {
    return this.http.get<PostResponse>(`${this.url}/${id}`);
  }

  create(request: CreatePostRequest): Observable<PostResponse> {
    return this.http.post<PostResponse>(this.url, request);
  }

  update(id: number, request: CreatePostRequest): Observable<PostResponse> {
    return this.http.put<PostResponse>(`${this.url}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  countByAuthor(username: string): Observable<number> {
    return this.http.get<number>(`${this.url}/count`, {
      params: new HttpParams().set('username', username)
    });
  }
}
