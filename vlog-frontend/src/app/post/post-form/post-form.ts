import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { PostService } from '../post.service';
import { CreatePostRequest } from '../post.model';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-post-form',
  imports: [ReactiveFormsModule],
  templateUrl: './post-form.html',
  styleUrl: './post-form.css'
})
export class PostForm {

  private readonly postService = inject(PostService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly postId = signal<number | null>(null);
  readonly isEdit = signal(false);
  readonly loading = signal(false);
  readonly submitting = signal(false);
  readonly formError = signal<string | null>(null);
  readonly fieldErrors = signal<Record<string, string>>({});

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    body: ['', [Validators.required]],
    postDate: [PostForm.today(), [Validators.required]]
  });

  constructor() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.postId.set(Number(idParam));
      this.isEdit.set(true);
      this.loadExisting(Number(idParam));
    }
  }

  private static today(): string {
    const now = new Date();
    const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 10);
  }

  private loadExisting(id: number): void {
    this.loading.set(true);

    this.postService.findById(id).subscribe({
      next: post => {
        this.loading.set(false);

        if (!this.auth.canModify(post.authorUsername)) {
          this.formError.set('You can only edit your own vlogs');
          this.form.disable();
          return;
        }

        this.form.setValue({
          title: post.title,
          body: post.body,
          postDate: post.postDate
        });
      },
      error: err => {
        this.loading.set(false);
        this.formError.set(err.error?.detail ?? 'Could not load that vlog');
      }
    });
  }

  serverError(field: string): string | undefined {
    return this.fieldErrors()[field];
  }

  submit(): void {
    this.formError.set(null);
    this.fieldErrors.set({});

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const request: CreatePostRequest = this.form.getRawValue();

    const call = this.isEdit()
      ? this.postService.update(this.postId()!, request)
      : this.postService.create(request);

    call.subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigate(['/']);
      },
      error: err => {
        this.submitting.set(false);
        if (err.error?.errors) {
          this.fieldErrors.set(err.error.errors);
          this.formError.set(err.error.detail ?? 'Please fix the errors below');
        } else {
          this.formError.set(err.error?.detail ?? 'Could not save the vlog');
        }
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/']);
  }
}
