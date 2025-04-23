import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

import { AuthContext } from '../../core/contexts/auth.context';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <div class="login-container">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">Login to Andah</h3>
        </div>
        <div class="card-body">
          <div *ngIf="authContext.error()" class="alert alert-danger">
            {{ authContext.error() }}
            <button type="button" class="close" (click)="authContext.clearError()">
              <span>&times;</span>
            </button>
          </div>

          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
            <div class="form-group">
              <label for="email">Email</label>
              <input 
                type="email" 
                class="form-control" 
                id="email" 
                formControlName="email" 
                placeholder="Enter your email"
                [ngClass]="{'is-invalid': submitted && f.email.errors}"
              >
              <div *ngIf="submitted && f.email.errors" class="invalid-feedback">
                <div *ngIf="f.email.errors.required">Email is required</div>
                <div *ngIf="f.email.errors.email">Please enter a valid email</div>
              </div>
            </div>

            <div class="form-group">
              <label for="password">Password</label>
              <input 
                type="password" 
                class="form-control" 
                id="password" 
                formControlName="password" 
                placeholder="Enter your password"
                [ngClass]="{'is-invalid': submitted && f.password.errors}"
              >
              <div *ngIf="submitted && f.password.errors" class="invalid-feedback">
                <div *ngIf="f.password.errors.required">Password is required</div>
              </div>
            </div>

            <div class="form-group form-check">
              <input type="checkbox" class="form-check-input" id="rememberMe" formControlName="rememberMe">
              <label class="form-check-label" for="rememberMe">Remember me</label>
            </div>

            <button 
              type="submit" 
              class="btn btn-primary btn-block" 
              [disabled]="authContext.isLoading()"
            >
              <span *ngIf="authContext.isLoading()" class="spinner-border spinner-border-sm mr-1"></span>
              Login
            </button>
          </form>

          <div class="alternative-actions mt-3">
            <p>
              <a (click)="showOtpLogin()">Login with verification code</a>
            </p>
            <p>
              <a (click)="forgotPassword()">Forgot your password?</a>
            </p>
            <p>
              Don't have an account? <a routerLink="/register">Register now</a>
            </p>
          </div>

          <!-- OTP Login Form (conditionally displayed) -->
          <div *ngIf="showOtp" class="mt-4">
            <h4>Login with Verification Code</h4>
            <form [formGroup]="otpForm" (ngSubmit)="onOtpSubmit()">
              <div class="form-group">
                <label for="otpEmail">Email</label>
                <input 
                  type="email" 
                  class="form-control" 
                  id="otpEmail" 
                  formControlName="email" 
                  placeholder="Enter your email"
                >
              </div>

              <div class="form-group">
                <label for="otp">Verification Code</label>
                <input 
                  type="text" 
                  class="form-control" 
                  id="otp" 
                  formControlName="otp" 
                  placeholder="Enter verification code"
                >
              </div>

              <div class="form-group">
                <button type="button" class="btn btn-link" (click)="requestOtp()">
                  Request verification code
                </button>
              </div>

              <button 
                type="submit" 
                class="btn btn-primary btn-block" 
                [disabled]="authContext.isLoading() || otpForm.invalid"
              >
                <span *ngIf="authContext.isLoading()" class="spinner-border spinner-border-sm mr-1"></span>
                Login with Code
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 80vh;
      padding: 20px;
    }
    .card {
      width: 100%;
      max-width: 450px;
      box-shadow: 0 4px 8px rgba(0,0,0,0.1);
    }
    .alternative-actions {
      text-align: center;
      border-top: 1px solid #eee;
      padding-top: 15px;
    }
    .alternative-actions a {
      cursor: pointer;
      color: #007bff;
      text-decoration: none;
    }
    .alternative-actions a:hover {
      text-decoration: underline;
    }
  `]
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  otpForm!: FormGroup;
  submitted = false;
  showOtp = false;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    public authContext: AuthContext
  ) {}

  ngOnInit(): void {
    // If already logged in, redirect to home
    if (this.authContext.isAuthenticated()) {
      this.router.navigate(['/']);
    }

    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      rememberMe: [false]
    });

    this.otpForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      otp: ['', Validators.required]
    });
  }

  // Convenience getter for easy access to form fields
  get f() { return this.loginForm.controls; }

  onSubmit(): void {
    this.submitted = true;

    // Stop here if form is invalid
    if (this.loginForm.invalid) {
      return;
    }

    this.authContext.login({
      email: this.f['email'].value,
      password: this.f['password'].value
    }).subscribe(success => {
      if (success) {
        this.router.navigate(['/']);
      }
    });
  }

  showOtpLogin(): void {
    this.showOtp = true;
    if (this.loginForm.get('email')?.value) {
      this.otpForm.get('email')?.setValue(this.loginForm.get('email')?.value);
    }
  }

  requestOtp(): void {
    const email = this.otpForm.get('email')?.value;
    if (!email) {
      this.authContext.error.set('Please enter your email address');
      return;
    }

    this.authContext.generateOtp(email).subscribe(success => {
      if (success) {
        // Show success message
        this.authContext.error.set(null);
        alert('Verification code has been sent to your email');
      }
    });
  }

  onOtpSubmit(): void {
    if (this.otpForm.invalid) {
      return;
    }

    const email = this.otpForm.get('email')?.value;
    const otp = this.otpForm.get('otp')?.value;

    this.authContext.loginWithOtp(email, otp).subscribe(success => {
      if (success) {
        this.router.navigate(['/']);
      }
    });
  }

  forgotPassword(): void {
    this.router.navigate(['/reset-password']);
  }
}
