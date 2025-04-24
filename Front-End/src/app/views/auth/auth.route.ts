import { Route } from '@angular/router'
import { SignInComponent } from './sign-in/sign-in.component'
import { SignUpComponent } from './sign-up/sign-up.component'
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component'
import { TwoFactorAuthComponent } from './two-factor-auth/two-factor-auth.component'
import { LoginComponent } from '../../components/auth/login.component'
import { RegisterComponent } from '../../components/auth/register.component'

export const AUTH_ROUTES: Route[] = [
  // Use template pages as primary routes
  { path: '', redirectTo: 'sign-in', pathMatch: 'full' },
  { path: 'sign-in', component: SignInComponent, data: { title: 'Sign In' } },
  { path: 'sign-up', component: SignUpComponent, data: { title: 'Sign Up' } },
  
  // Keep old routes for backward compatibility
  { path: 'login', component: SignInComponent, data: { title: 'Login' } },
  { path: 'register', component: SignUpComponent, data: { title: 'Register' } },

  {
    path: 'forgot-password',
    component: ForgotPasswordComponent,
    data: { title: 'Forgot Password' },
  },
  {
    path: 'two-factor-auth',
    component: TwoFactorAuthComponent,
    data: { title: 'Two Factor Authentication' },
  },
]
