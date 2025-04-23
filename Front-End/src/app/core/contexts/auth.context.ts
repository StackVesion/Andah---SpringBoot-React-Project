import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

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
  }

  // Check if user is authenticated using stored token
  private checkAuthStatus(): void {
    const isAuthenticated = this.authService.isAuthenticated;
    const currentUser = this.authService.currentUser;

    this.isAuthenticated.set(isAuthenticated);
    this.currentUser.set(currentUser);
  }

  // Login with email and password
  login(credentials: LoginRequest): Observable<boolean> {
    this.isLoading.set(true);
    this.error.set(null);

    return this.authService.login(credentials).pipe(
      map(response => {
        this.isAuthenticated.set(true);
        this.currentUser.set(this.authService.currentUser);
        return true;
      }),
      catchError(error => {
        this.error.set(error.message || 'Login failed');
        return of(false);
      }),
      tap(() => this.isLoading.set(false))
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
        return of(false);
      }),
      tap(() => this.isLoading.set(false))
    );
  }

  // Register a new user
  register(request: RegisterRequest): Observable<boolean> {
    this.isLoading.set(true);
    this.error.set(null);

    return this.authService.register(request).pipe(
      map(response => {
        this.isAuthenticated.set(true);
        this.currentUser.set(this.authService.currentUser);
        return true;
      }),
      catchError(error => {
        this.error.set(error.message || 'Registration failed');
        return of(false);
      }),
      tap(() => this.isLoading.set(false))
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
        return of(false);
      }),
      tap(() => this.isLoading.set(false))
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
        return of(false);
      }),
      tap(() => this.isLoading.set(false))
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
        return of(false);
      }),
      tap(() => this.isLoading.set(false))
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
        return of(false);
      }),
      tap(() => this.isLoading.set(false))
    );
  }

  // Logout the current user
  logout(): void {
    this.authService.logout();
    this.isAuthenticated.set(false);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  // Clear any error message
  clearError(): void {
    this.error.set(null);
  }
}
