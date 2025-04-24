import { Inject, Injectable } from '@angular/core'
import { ActivatedRoute, Router } from '@angular/router'
import { AuthenticationService } from '@core/services/auth.service'
import { Actions, createEffect, ofType } from '@ngrx/effects'
import { of } from 'rxjs'
import { catchError, exhaustMap, map } from 'rxjs/operators'
import {
  login,
  loginFailure,
  loginSuccess,
  logout,
  logoutSuccess,
} from './authentication.actions'

@Injectable()
export class AuthenticationEffects {
  login$ = createEffect(() =>
    this.actions$.pipe(
      ofType(login),
      exhaustMap(({ email, password }) => {
        // Create credentials object to match the expected API
        const credentials = { email, password };
        
        return this.AuthenticationService.login(credentials).pipe(
          map((response) => {
            if (response) {
              const returnUrl =
                this.route.snapshot.queryParams['returnUrl'] || '/'
              this.router.navigateByUrl(returnUrl)
            }
            
            // Extract user from response - using cast for type compatibility
            const user = {
              id: response.userId,
              email: response.username,
              role: response.role as any, // Using 'as any' to avoid type issues
              ...(response.user || {})
            };
            
            return loginSuccess({ user })
          }),
          catchError((error) => of(loginFailure({ error })))
        )
      })
    )
  )

  logout$ = createEffect(() =>
    this.actions$.pipe(
      ofType(logout),
      exhaustMap(() => {
        this.AuthenticationService.logout()
        this.router.navigate(['/login'])
        return of(logoutSuccess())
      })
    )
  )

  constructor(
    @Inject(Actions) private actions$: Actions,
    private AuthenticationService: AuthenticationService,
    private router: Router,
    private route: ActivatedRoute
  ) {}
}
