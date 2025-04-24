import { Injectable } from '@angular/core'
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http'
import { Observable, throwError, BehaviorSubject } from 'rxjs'
import { catchError, filter, take, switchMap } from 'rxjs/operators'

import { AuthenticationService } from '@core/services/auth.service'
import { AuthContext } from '@core/contexts/auth.context'

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

  constructor(
    private authenticationService: AuthenticationService,
    private authContext: AuthContext
  ) {}

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    // Don't add token for auth endpoints
    if (this.isAuthRequest(request.url)) {
      return next.handle(request);
    }

    // Add auth token if available
    const token = this.authenticationService.getToken();
    if (token) {
      request = this.addToken(request, token);
    }

    // Process the request and handle errors
    return next.handle(request).pipe(
      catchError(error => {
        if (error instanceof HttpErrorResponse && error.status === 401) {
          return this.handle401Error(request, next);
        }
        return throwError(() => error);
      })
    );
  }

  private addToken(request: HttpRequest<any>, token: string): HttpRequest<any> {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  private isAuthRequest(url: string): boolean {
    // Skip token for these endpoints
    const authEndpoints = [
      '/auth/login',
      '/auth/register',
      '/auth/login-alternative',
      '/auth/reset-password-request',
      '/auth/reset-password',
      '/auth/generate-otp',
      '/auth/login-with-otp'
    ];
    
    return authEndpoints.some(endpoint => url.includes(endpoint));
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      // Try refreshing the token
      return this.authContext.refreshToken().pipe(
        switchMap(success => {
          this.isRefreshing = false;
          
          if (success) {
            this.refreshTokenSubject.next(this.authenticationService.getToken());
            return next.handle(this.addToken(request, this.authenticationService.getToken() || ''));
          }
          
          // If refresh fails, redirect to login
          this.authContext.logout();
          return throwError(() => new Error('Session expired. Please log in again.'));
        }),
        catchError(error => {
          this.isRefreshing = false;
          this.authContext.logout();
          return throwError(() => error);
        })
      );
    }

    // Wait for token refresh to complete
    return this.refreshTokenSubject.pipe(
      filter(token => token !== null),
      take(1),
      switchMap(token => next.handle(this.addToken(request, token)))
    );
  }
}
