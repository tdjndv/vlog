import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, switchMap } from 'rxjs';

import { PostService } from '../post.service';
import { PostFilter, PostResponse } from '../post.model';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-post-list',
  imports: [ReactiveFormsModule, DatePipe, RouterLink],
  templateUrl: './post-list.html',
  styleUrl: './post-list.css'
})
export class PostList {

  private readonly postService = inject(PostService);
  private readonly auth = inject(AuthService);

  readonly usernameCtrl = new FormControl('', { nonNullable: true });
  readonly keywordCtrl  = new FormControl('', { nonNullable: true });
  readonly fromCtrl     = new FormControl('', { nonNullable: true });
  readonly toCtrl       = new FormControl('', { nonNullable: true });

  readonly pageIndex = signal(0);
  readonly posts = signal<PostResponse[]>([]);
  readonly totalElements = signal(0);
  readonly totalPages = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  private readonly search$ = new Subject<void>();
  private debounce?: ReturnType<typeof setTimeout>;

  constructor() {
    this.search$.pipe(
      switchMap(() => {
        this.loading.set(true);
        this.error.set(null);
        return this.postService.search(this.currentFilter(),
          { page: this.pageIndex(), size: 10 });
      }),
      takeUntilDestroyed()
    ).subscribe({
      next: page => {
        this.posts.set(page.content);
        this.totalElements.set(page.totalElements);
        this.totalPages.set(page.totalPages);
        this.loading.set(false);
      },
      error: err => {
        this.posts.set([]);
        this.totalElements.set(0);
        this.totalPages.set(0);
        this.error.set(err.error?.detail ?? 'Failed to load posts');
        this.loading.set(false);
      }
    });

    this.load();
  }

  private currentFilter(): PostFilter {
    return {
      username: this.usernameCtrl.value,
      keyword: this.keywordCtrl.value,
      from: this.fromCtrl.value,
      to: this.toCtrl.value
    };
  }

  canModify(authorUsername: string): boolean {
    return this.auth.canModify(authorUsername);
  }

  onSearchInput(): void {
    clearTimeout(this.debounce);
    this.debounce = setTimeout(() => this.onFilterChange(), 300);
  }

  onFilterChange(): void {
    this.pageIndex.set(0);
    this.load();
  }

  load(): void {
    this.search$.next();
  }

  remove(id: number): void {
    if (!confirm('Delete this vlog?')) return;

    this.postService.delete(id).subscribe({
      next: () => this.load(),
      error: err => this.error.set(err.error?.detail ?? 'Could not delete')
    });
  }

  nextPage(): void {
    if (this.pageIndex() < this.totalPages() - 1) {
      this.pageIndex.update(i => i + 1);
      this.load();
    }
  }

  previousPage(): void {
    if (this.pageIndex() > 0) {
      this.pageIndex.update(i => i - 1);
      this.load();
    }
  }

  clearFilters(): void {
    this.usernameCtrl.setValue('');
    this.keywordCtrl.setValue('');
    this.fromCtrl.setValue('');
    this.toCtrl.setValue('');
    this.onFilterChange();
  }
}
