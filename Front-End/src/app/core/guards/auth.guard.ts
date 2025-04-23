import { inject } from '@angular/core'
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
} from '@angular/router'

import { AuthContext } from '../contexts/auth.context'

export const AuthGuard: CanActivateFn = (
  next: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {
  const authContext = inject(AuthContext)
  const router = inject(Router)

  // Check if user is authenticated using the AuthContext
  if (authContext.isAuthenticated()) {
    return true
  }

  // Redirect to login page with return URL
  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url },
  })
}
