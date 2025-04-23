import { Route } from '@angular/router'
import { SignInComponent } from './sign-in/sign-in.component'
import { SignUpComponent } from './sign-up/sign-up.component'
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component'
import { TwoFactorAuthComponent } from './two-factor-auth/two-factor-auth.component'
import { LoginComponent } from '../../components/auth/login.component'
import { RegisterComponent } from '../../components/auth/register.component'

export const AUTH_ROUTES: Route[] = [
  // Original routes
  { path: 'sign-in', component: SignInComponent, data: { title: 'Sign In' } },
  { path: 'sign-up', component: SignUpComponent, data: { title: 'Sign Up' } },
  
  // New routes with modern auth context
  { path: 'login', component: LoginComponent, data: { title: 'Login' } },
  { path: 'register', component: RegisterComponent, data: { title: 'Register' } },

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
