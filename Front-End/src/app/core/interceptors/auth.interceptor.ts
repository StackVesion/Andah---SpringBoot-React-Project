import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap, finalize } from 'rxjs/operators';

import { AuthenticationService } from '../services/auth.service';
import { AuthContext } from '../contexts/auth.context';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

  constructor(
    private authService: AuthenticationService,
    private authContext: AuthContext
  ) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // For debugging purposes
    console.log(`Intercepting request to: ${request.url}`);
    
    // Don't add token for auth endpoints
    if (this.isAuthRequest(request.url)) {
      console.log('Auth request detected, skipping token addition');
      return next.handle(request);
    }

    // Add auth token if available
    const token = this.authService.getToken();
    if (token) {
      console.log('Adding token to request');
      request = this.addToken(request, token);
    } else {
      console.log('No token available for request');
    }

    // Process the request and handle errors
    return next.handle(request).pipe(
      catchError(error => {
        if (error instanceof HttpErrorResponse) {
          console.log(`HTTP error: ${error.status} for ${request.url}`);
          
          if (error.status === 401 && !this.isAuthRequest(request.url)) {
            console.log('401 Unauthorized error, attempting token refresh');
            return this.handle401Error(request, next);
          }
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
    // Skip token for these endpoints - API Gateway paths
    const authEndpoints = [
      '/user-service/api/auth/login',
      '/user-service/api/auth/register',
      '/user-service/api/auth/refresh-token',
      '/user-service/api/auth/reset-password-request',
      '/user-service/api/auth/reset-password',
      '/user-service/api/auth/generate-otp',
      '/user-service/api/auth/login-with-otp',
      '/user-service/api/auth/logout'
    ];
    
    // Also check for simplified paths that may be used in the frontend
    if (url.includes('/auth/login') || 
        url.includes('/auth/register') || 
        url.includes('/auth/logout')) {
      return true;
    }
    
    return authEndpoints.some(endpoint => url.includes(endpoint));
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Skip token refresh for auth endpoints that failed with 401
    if (this.isAuthRequest(request.url)) {
      console.log('Auth endpoint failed with 401, not attempting token refresh');
      return throwError(() => new Error('Authentication failed'));
    }
    
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      const refreshToken = this.authService.getRefreshToken();
      if (!refreshToken) {
        console.log('No refresh token available, redirecting to login');
        this.isRefreshing = false;
        this.authContext.logout();
        return throwError(() => new Error('No refresh token available. Please log in again.'));
      }

      console.log('Attempting to refresh token');
      // Try refreshing the token
      return this.authContext.refreshToken().pipe(
        switchMap(success => {
          this.isRefreshing = false;
          
          if (success) {
            const newToken = this.authService.getToken();
            console.log('Token refresh successful, adding new token to request');
            this.refreshTokenSubject.next(newToken);
            return next.handle(this.addToken(request, newToken || ''));
          }
          
          // If refresh fails, redirect to login
          console.log('Token refresh failed, redirecting to login');
          this.authContext.logout();
          return throwError(() => new Error('Session expired. Please log in again.'));
        }),
        catchError(error => {
          console.error('Error during token refresh:', error);
          this.isRefreshing = false;
          this.authContext.logout();
          return throwError(() => error);
        }),
        finalize(() => {
          this.isRefreshing = false;
        })
      );
    }

    // Wait for token refresh to complete
    console.log('Waiting for token refresh to complete');
    return this.refreshTokenSubject.pipe(
      filter(token => token !== null),
      take(1),
      switchMap(token => {
        console.log('Using newly refreshed token for request');
        return next.handle(this.addToken(request, token));
      })
    );
  }
}
