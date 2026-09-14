export type Role = 'USER' | 'MANAGER';

export interface PostResponse {
  id: number;
  title: string;
  body: string;
  postDate: string;
  authorUsername: string;
  authorRole: Role;
  createdAt: string;
  updatedAt: string | null;
}

export interface CreatePostRequest {
  title: string;
  body: string;
  postDate: string;
}

export interface PostFilter {
  username?: string;
  keyword?: string;
  date?: string;
  from?: string;
  to?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
