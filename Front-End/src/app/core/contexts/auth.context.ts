import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map, tap, finalize } from 'rxjs/operators';

import { AuthenticationService, LoginRequest, RegisterRequest } from '../services/auth.service';
import { User } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthContext {
  // Signal-based reactive state for auth status
  public isAuthenticated = signal<boolean>(false);
  public currentUser = signal<User | null>(null);
  public isLoading = signal<boolean>(false);
  public error = signal<string | null>(null);

  constructor(
    private authService: AuthenticationService,
    private router: Router
  ) {
    // Initialize auth state from stored session
    this.checkAuthStatus();
    console.log('AuthContext initialized, authenticated:', this.isAuthenticated());
  }

  // Check if user is authenticated using stored token
  private checkAuthStatus(): void {
    const isAuthenticated = this.authService.isAuthenticated;
    const currentUser = this.authService.currentUser;

    console.log('Checking auth status:', { isAuthenticated, currentUser });
    this.isAuthenticated.set(isAuthenticated);
    this.currentUser.set(currentUser);
  }

  // Refresh token if it's available
  refreshToken(): Observable<boolean> {
    if (!this.authService.getRefreshToken()) {
      console.warn('No refresh token available, cannot refresh');
      return throwError(() => new Error('No refresh token available'));
    }

    console.log('Attempting to refresh token');
    this.isLoading.set(true);
    this.error.set(null);

    return this.authService.refreshAccessToken().pipe(
      map(response => {
        console.log('Token refresh successful');
        this.isAuthenticated.set(true);
        this.currentUser.set(this.authService.currentUser);
        return true;
      }),
      catchError(error => {
        console.error('Token refresh failed:', error);
        this.error.set(error.message || 'Session expired');
        this.logout();
        return throwError(() => error);
      }),
      finalize(() => this.isLoading.set(false))
    );
  }

  // Login with email and password
  login(credentials: LoginRequest): Observable<boolean> {
    console.log('AuthContext: Attempting login with credentials:', { ...credentials, password: '***' });
    this.isLoading.set(true);
    this.error.set(null);

    return this.authService.login(credentials).pipe(
      map(response => {
        console.log('AuthContext: Login successful');
        this.isAuthenticated.set(true);
        this.currentUser.set(this.authService.currentUser);
        return true;
      }),
      catchError(error => {
        console.error('AuthContext: Login failed:', error);
        this.error.set(error.message || 'Login failed');
        return throwError(() => error);
      }),
      finalize(() => this.isLoading.set(false))
    );
  }

  // Alternative login method for troubleshooting
  loginAlternative(credentials: LoginRequest): Observable<boolean> {
    this.isLoading.set(true);
    this.error.set(null);

    return this.authService.loginAlternative(credentials).pipe(
      map(response => {
        this.isAuthenticated.set(true);
        this.currentUser.set(this.authService.currentUser);
        return true;
      }),
      catchError(error => {
        this.error.set(error.message || 'Login failed');
        return throwError(() => error);
      }),
      finalize(() => this.isLoading.set(false))
    );
  }

  // Register a new user
  register(request: RegisterRequest): Observable<boolean> {
    console.log('AuthContext: Attempting registration with data:', { ...request, password: '***' });
    this.isLoading.set(true);
    this.error.set(null);

    return this.authService.register(request).pipe(
      map(response => {
        console.log('AuthContext: Registration successful');
        this.isAuthenticated.set(true);
        this.currentUser.set(this.authService.currentUser);
        return true;
      }),
      catchError(error => {
        console.error('AuthContext: Registration failed:', error);
        this.error.set(error.message || 'Registration failed');
        return throwError(() => error);
      }),
      finalize(() => this.isLoading.set(false))
    );
  }

  // Login with OTP
  loginWithOtp(email: string, otp: string): Observable<boolean> {
    this.isLoading.set(true);
    this.error.set(null);

    return this.authService.loginWithOtp(email, otp).pipe(
      map(response => {
        this.isAuthenticated.set(true);
        this.currentUser.set(this.authService.currentUser);
        return true;
      }),
      catchError(error => {
        this.error.set(error.message || 'OTP login failed');
        return throwError(() => error);
      }),
      finalize(() => this.isLoading.set(false))
    );
  }

  // Request password reset
  requestPasswordReset(email: string): Observable<boolean> {
    this.isLoading.set(true);
    this.error.set(null);

    return this.authService.requestPasswordReset(email).pipe(
      map(() => true),
      catchError(error => {
        this.error.set(error.message || 'Password reset request failed');
        return throwError(() => error);
      }),
      finalize(() => this.isLoading.set(false))
    );
  }

  // Reset password with token
  resetPassword(token: string, newPassword: string): Observable<boolean> {
    this.isLoading.set(true);
    this.error.set(null);

    return this.authService.resetPassword(token, newPassword).pipe(
      map(() => true),
      catchError(error => {
        this.error.set(error.message || 'Password reset failed');
        return throwError(() => error);
      }),
      finalize(() => this.isLoading.set(false))
    );
  }

  // Generate OTP for email verification
  generateOtp(email: string): Observable<boolean> {
    this.isLoading.set(true);
    this.error.set(null);

    return this.authService.generateOtp(email).pipe(
      map(() => true),
      catchError(error => {
        this.error.set(error.message || 'OTP generation failed');
        return throwError(() => error);
      }),
      finalize(() => this.isLoading.set(false))
    );
  }

  // Logout the current user
  logout(): void {
    console.log('AuthContext: Logging out user');
    this.authService.logout();
    this.isAuthenticated.set(false);
    this.currentUser.set(null);
    this.router.navigate(['/auth/sign-in']);
  }

  // Clear any error message
  clearError(): void {
    this.error.set(null);
  }
}
