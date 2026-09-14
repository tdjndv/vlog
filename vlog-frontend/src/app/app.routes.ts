import { Routes } from '@angular/router';
import { PostList } from './post/post-list/post-list';
import { PostForm } from './post/post-form/post-form';
import { Login } from './auth/login/login';
import { Signup } from './auth/signup/signup';
import { authGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: '', component: PostList },
  { path: 'login', component: Login },
  { path: 'signup', component: Signup },
  { path: 'new', component: PostForm, canActivate: [authGuard] },
  { path: 'edit/:id', component: PostForm, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
