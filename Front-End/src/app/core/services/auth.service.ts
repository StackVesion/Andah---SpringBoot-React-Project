import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { BehaviorSubject, Observable, throwError } from 'rxjs'
import { catchError, map, tap } from 'rxjs/operators'

import { User } from '@core/models/auth.model'
import { CookieService } from 'ngx-cookie-service'
import { environment } from '../../../environments/environment'

export interface AuthResponse {
  token: string;
  userId: string;
  username: string;
  role: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  name: string;
  email: string;
  password: string;
  phoneNumber: string;
}

@Injectable({ providedIn: 'root' })
export class AuthenticationService {
  private readonly API_URL = environment.apiUrl || 'http://localhost:8080/api';
  private userSubject: BehaviorSubject<User | null>;
  public user$: Observable<User | null>;

  public readonly authSessionKey = '_ANDAH_AUTH_SESSION_KEY_';
  public readonly userDataKey = '_ANDAH_USER_DATA_KEY_';

  constructor(
    private http: HttpClient,
    private cookieService: CookieService
  ) {
    // Initialize user from localStorage
    const userData = localStorage.getItem(this.userDataKey);
    this.userSubject = new BehaviorSubject<User | null>(
      userData ? JSON.parse(userData) : null
    );
    this.user$ = this.userSubject.asObservable();
  }

  // Get current user value without subscribing to the user$ Observable
  public get currentUser(): User | null {
    return this.userSubject.value;
  }

  // Check if user is authenticated
  public get isAuthenticated(): boolean {
    return !!this.currentUser && !!this.getToken();
  }

  // Get authentication token
  public getToken(): string | null {
    return this.cookieService.get(this.authSessionKey) || null;
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/auth/login`, credentials)
      .pipe(
        tap(response => this.handleAuthResponse(response)),
        catchError(error => {
          console.error('Login failed:', error);
          return throwError(() => new Error(error.error?.message || 'Login failed. Please check your credentials.'));
        })
      );
  }

  // Alternative method for troubleshooting login
  loginAlternative(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/auth/login-alternative`, credentials)
      .pipe(
        tap(response => this.handleAuthResponse(response)),
        catchError(error => {
          console.error('Alternative login failed:', error);
          return throwError(() => new Error(error.error?.message || 'Login failed. Please check your credentials.'));
        })
      );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/auth/register`, request)
      .pipe(
        tap(response => this.handleAuthResponse(response)),
        catchError(error => {
          console.error('Registration failed:', error);
          return throwError(() => new Error(error.error?.message || 'Registration failed. Please try again.'));
        })
      );
  }

  // Send OTP for email verification or password reset
  generateOtp(email: string): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/auth/generate-otp`, { email })
      .pipe(
        catchError(error => {
          console.error('OTP generation failed:', error);
          return throwError(() => new Error(error.error?.message || 'Failed to send verification code.'));
        })
      );
  }

  // Login with OTP
  loginWithOtp(email: string, otp: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/auth/login-with-otp`, { email, otp })
      .pipe(
        tap(response => this.handleAuthResponse(response)),
        catchError(error => {
          console.error('OTP login failed:', error);
          return throwError(() => new Error(error.error?.message || 'Login with verification code failed.'));
        })
      );
  }

  // Request password reset
  requestPasswordReset(email: string): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/auth/reset-password-request`, { email })
      .pipe(
        catchError(error => {
          console.error('Password reset request failed:', error);
          return throwError(() => new Error(error.error?.message || 'Failed to request password reset.'));
        })
      );
  }

  // Reset password with token
  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/auth/reset-password`, { token, newPassword })
      .pipe(
        catchError(error => {
          console.error('Password reset failed:', error);
          return throwError(() => new Error(error.error?.message || 'Failed to reset password.'));
        })
      );
  }

  logout(): void {
    // Remove user data and token
    this.cookieService.delete(this.authSessionKey, '/');
    localStorage.removeItem(this.userDataKey);
    this.userSubject.next(null);
  }

  // Handle authentication response
  private handleAuthResponse(response: AuthResponse): void {
    // Store token in cookie (with secure settings for production)
    this.cookieService.set(
      this.authSessionKey,
      response.token,
      {
        expires: 1, // 1 day
        path: '/',
        secure: environment.production,
        sameSite: 'Strict'
      }
    );

    // Store user data in localStorage
    const userData = {
      id: response.userId,
      email: response.username,
      role: response.role,
      ...response.user
    };
    
    localStorage.setItem(this.userDataKey, JSON.stringify(userData));
    this.userSubject.next(userData as User);
  }
}
