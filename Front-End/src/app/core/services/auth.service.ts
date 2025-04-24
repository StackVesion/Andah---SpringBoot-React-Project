import { Injectable } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { BehaviorSubject, Observable, throwError } from 'rxjs'
import { catchError, map, tap } from 'rxjs/operators'

import { User } from '@core/models/auth.model'
import { CookieService } from 'ngx-cookie-service'
import { environment } from '../../../environments/environment'

export interface AuthResponse {
  token: string;
  refreshToken?: string;
  userId: string;
  username: string;
  email: string;
  role: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name?: string;
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phoneNumber: string;
}

@Injectable({ providedIn: 'root' })
export class AuthenticationService {
  private readonly API_URL = environment.apiUrl || 'http://localhost:8080';
  private readonly AUTH_ENDPOINT = `${this.API_URL}/user-service/api/auth`; 
  
  private userSubject: BehaviorSubject<User | null>;
  public user$: Observable<User | null>;

  public readonly authSessionKey = '_ANDAH_AUTH_SESSION_KEY_';
  public readonly refreshTokenKey = '_ANDAH_REFRESH_TOKEN_KEY_';
  public readonly userDataKey = '_ANDAH_USER_DATA_KEY_';

  constructor(
    private http: HttpClient,
    private cookieService: CookieService
  ) {
    const userData = localStorage.getItem(this.userDataKey);
    this.userSubject = new BehaviorSubject<User | null>(
      userData ? JSON.parse(userData) : null
    );
    this.user$ = this.userSubject.asObservable();
  }

  public get currentUser(): User | null {
    return this.userSubject.value;
  }

  public get isAuthenticated(): boolean {
    return !!this.currentUser && !!this.getToken();
  }

  public getToken(): string | null {
    return this.cookieService.get(this.authSessionKey) || null;
  }

  public getRefreshToken(): string | null {
    return this.cookieService.get(this.refreshTokenKey) || null;
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    console.log(`Attempting login via ${this.AUTH_ENDPOINT}/login with:`, {...credentials, password: '[REDACTED]'});
    return this.http.post<AuthResponse>(`${this.AUTH_ENDPOINT}/login`, credentials)
      .pipe(
        tap(response => this.storeAuthData(response)),
        catchError(error => {
          console.error('Login failed:', error);
          let errorMsg = 'Login failed. Please check your credentials.';
          if (error.error?.message) {
            errorMsg = error.error.message;
          } else if (error.status === 401) {
            errorMsg = 'Invalid email or password.';
          } else if (error.status === 0) {
            errorMsg = 'Unable to connect to the server. Please check your internet connection.';
          }
          return throwError(() => new Error(errorMsg));
        })
      );
  }

  loginAlternative(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.AUTH_ENDPOINT}/login-alternative`, credentials)
      .pipe(
        tap(response => this.storeAuthData(response)),
        catchError(error => {
          console.error('Alternative login failed:', error);
          return throwError(() => new Error(error.error?.message || 'Login failed. Please check your credentials.'));
        })
      );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    console.log(`Attempting registration via ${this.AUTH_ENDPOINT}/register with:`, {...request, password: '[REDACTED]'});
    if (!request.name) {
      request.name = `${request.firstName} ${request.lastName}`;
    }
    
    return this.http.post<AuthResponse>(`${this.AUTH_ENDPOINT}/register`, request)
      .pipe(
        tap(response => this.storeAuthData(response)),
        catchError(error => {
          console.error('Registration failed:', error);
          let errorMsg = 'Registration failed. Please try again.';
          if (error.error?.message) {
            errorMsg = error.error.message;
          } else if (error.status === 409) {
            errorMsg = 'This email is already registered. Please use a different email or login.';
          } else if (error.status === 400) {
            errorMsg = 'Invalid registration data. Please check your information.';
          } else if (error.status === 0) {
            errorMsg = 'Unable to connect to the server. Please check your internet connection.';
          }
          return throwError(() => new Error(errorMsg));
        })
      );
  }

  registerWithDebug(request: RegisterRequest): Observable<any> {
    console.log(`Debug registration via ${this.AUTH_ENDPOINT}/register with:`, {...request, password: '[REDACTED]'});
    
    return this.http.post(`${this.AUTH_ENDPOINT}/register`, request)
      .pipe(
        tap(response => {
          console.log('Debug registration successful:', response);
        }),
        catchError(error => {
          console.error('Debug registration error details:', {
            status: error.status,
            statusText: error.statusText,
            error: error.error,
            message: error.message,
            url: `${this.AUTH_ENDPOINT}/register`
          });
          return throwError(() => error);
        })
      );
  }

  refreshAccessToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }
    
    return this.http.post<AuthResponse>(`${this.AUTH_ENDPOINT}/refresh-token`, { refreshToken })
      .pipe(
        tap(response => this.storeAuthData(response)),
        catchError(error => {
          console.error('Token refresh failed:', error);
          this.logout();
          return throwError(() => new Error('Your session has expired. Please login again.'));
        })
      );
  }

  generateOtp(email: string): Observable<any> {
    return this.http.post<any>(`${this.AUTH_ENDPOINT}/generate-otp`, { email })
      .pipe(
        catchError(error => {
          console.error('OTP generation failed:', error);
          return throwError(() => new Error(error.error?.message || 'Failed to send verification code.'));
        })
      );
  }

  loginWithOtp(email: string, otp: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.AUTH_ENDPOINT}/login-with-otp`, { email, otp })
      .pipe(
        tap(response => this.storeAuthData(response)),
        catchError(error => {
          console.error('OTP login failed:', error);
          return throwError(() => new Error(error.error?.message || 'Login with verification code failed.'));
        })
      );
  }

  requestPasswordReset(email: string): Observable<any> {
    return this.http.post<any>(`${this.AUTH_ENDPOINT}/reset-password-request`, { email })
      .pipe(
        catchError(error => {
          console.error('Password reset request failed:', error);
          return throwError(() => new Error(error.error?.message || 'Failed to request password reset.'));
        })
      );
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post<any>(`${this.AUTH_ENDPOINT}/reset-password`, { token, newPassword })
      .pipe(
        catchError(error => {
          console.error('Password reset failed:', error);
          return throwError(() => new Error(error.error?.message || 'Failed to reset password.'));
        })
      );
  }

  logout(): void {
    // Only attempt server-side logout if we have a token (we're authenticated)
    const token = this.getToken();
    if (token) {
      try {
        this.http.post(`${this.AUTH_ENDPOINT}/logout`, {}).subscribe({
          next: () => console.log('Server-side logout successful'),
          error: (err) => console.warn('Server-side logout failed:', err)
        });
      } catch (e) {
        console.warn('Error during server-side logout:', e);
      }
    } else {
      console.log('No token available, skipping server-side logout');
    }

    // Always perform client-side logout
    this.cookieService.delete(this.authSessionKey, '/');
    this.cookieService.delete(this.refreshTokenKey, '/');
    localStorage.removeItem(this.userDataKey);
    this.userSubject.next(null);
  }

  private storeAuthData(data: AuthResponse): void {
    if (data && data.token) {
      // Store tokens
      this.cookieService.set(
        this.authSessionKey,
        data.token,
        {
          expires: 1, // 1 day
          path: '/',
          secure: environment.production,
          sameSite: 'Strict'
        }
      );

      if (data.refreshToken) {
        this.cookieService.set(
          this.refreshTokenKey,
          data.refreshToken,
          {
            expires: 7, // 7 days
            path: '/',
            secure: environment.production,
            sameSite: 'Strict'
          }
        );
      }

      // Create user data object
      const userData = {
        id: data.userId,
        email: data.username,
        role: data.role,
        ...data.user
      };
      
      // Store user data in localStorage
      localStorage.setItem(this.userDataKey, JSON.stringify(userData));
      this.userSubject.next(userData as User);
    }
  }
}
