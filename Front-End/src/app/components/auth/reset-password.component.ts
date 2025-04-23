import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';

import { AuthContext } from '../../core/contexts/auth.context';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <div class="reset-password-container">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">{{ token ? 'Reset Your Password' : 'Request Password Reset' }}</h3>
        </div>
        <div class="card-body">
          <div *ngIf="authContext.error()" class="alert alert-danger">
            {{ authContext.error() }}
            <button type="button" class="close" (click)="authContext.clearError()">
              <span>&times;</span>
            </button>
          </div>

          <div *ngIf="success" class="alert alert-success">
            {{ success }}
          </div>

          <!-- Password reset request form -->
          <form *ngIf="!token" [formGroup]="requestForm" (ngSubmit)="onRequestSubmit()">
            <div class="form-group">
              <label for="email">Email</label>
              <input 
                type="email" 
                class="form-control" 
                id="email" 
                formControlName="email" 
                placeholder="Enter your email"
                [ngClass]="{'is-invalid': requestSubmitted && r['email'].errors}"
              >
              <div *ngIf="requestSubmitted && r['email'].errors" class="invalid-feedback">
                <div *ngIf="r['email'].errors?.['required']">Email is required</div>
                <div *ngIf="r['email'].errors?.['email']">Please enter a valid email</div>
              </div>
            </div>

            <button 
              type="submit" 
              class="btn btn-primary btn-block" 
              [disabled]="authContext.isLoading()"
            >
              <span *ngIf="authContext.isLoading()" class="spinner-border spinner-border-sm mr-1"></span>
              Send Reset Instructions
            </button>
          </form>

          <!-- Password reset form (with token) -->
          <form *ngIf="token" [formGroup]="resetForm" (ngSubmit)="onResetSubmit()">
            <div class="form-group">
              <label for="password">New Password</label>
              <input 
                type="password" 
                class="form-control" 
                id="password" 
                formControlName="password" 
                placeholder="Enter new password"
                [ngClass]="{'is-invalid': resetSubmitted && f['password'].errors}"
              >
              <div *ngIf="resetSubmitted && f['password'].errors" class="invalid-feedback">
                <div *ngIf="f['password'].errors?.['required']">Password is required</div>
                <div *ngIf="f['password'].errors?.['minlength']">Password must be at least 6 characters</div>
              </div>
            </div>

            <div class="form-group">
              <label for="confirmPassword">Confirm Password</label>
              <input 
                type="password" 
                class="form-control" 
                id="confirmPassword" 
                formControlName="confirmPassword" 
                placeholder="Confirm your password"
                [ngClass]="{'is-invalid': resetSubmitted && f['confirmPassword'].errors}"
              >
              <div *ngIf="resetSubmitted && f['confirmPassword'].errors" class="invalid-feedback">
                <div *ngIf="f['confirmPassword'].errors?.['required']">Password confirmation is required</div>
                <div *ngIf="f['confirmPassword'].errors?.['mustMatch']">Passwords must match</div>
              </div>
            </div>

            <button 
              type="submit" 
              class="btn btn-primary btn-block" 
              [disabled]="authContext.isLoading()"
            >
              <span *ngIf="authContext.isLoading()" class="spinner-border spinner-border-sm mr-1"></span>
              Reset Password
            </button>
          </form>

          <div class="mt-3 text-center">
            <a routerLink="/auth/login">Back to Login</a>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .reset-password-container {
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
  `]
})
export class ResetPasswordComponent implements OnInit {
  requestForm!: FormGroup;
  resetForm!: FormGroup;
  token: string | null = null;
  requestSubmitted = false;
  resetSubmitted = false;
  success: string | null = null;

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    public authContext: AuthContext
  ) {}

  ngOnInit(): void {
    // Get token from URL if present
    this.token = this.route.snapshot.queryParamMap.get('token');

    // Initialize request form
    this.requestForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]]
    });

    // Initialize reset form if token is present
    if (this.token) {
      this.resetForm = this.formBuilder.group({
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', Validators.required]
      }, {
        validator: this.passwordMatchValidator
      });
    }
  }

  // Convenience getters for form controls
  get r() { return this.requestForm.controls; }
  get f() { return this.resetForm ? this.resetForm.controls : {}; }

  // Custom validator to check if password and confirm password match
  passwordMatchValidator(formGroup: FormGroup) {
    const password = formGroup.get('password')?.value;
    const confirmPassword = formGroup.get('confirmPassword')?.value;

    if (password !== confirmPassword) {
      formGroup.get('confirmPassword')?.setErrors({ mustMatch: true });
    } else {
      formGroup.get('confirmPassword')?.setErrors(null);
    }
  }

  onRequestSubmit(): void {
    this.requestSubmitted = true;
    this.success = null;

    // Stop here if form is invalid
    if (this.requestForm.invalid) {
      return;
    }

    this.authContext.requestPasswordReset(this.r['email'].value).subscribe(success => {
      if (success) {
        this.success = 'Password reset instructions have been sent to your email.';
        this.requestForm.reset();
        this.requestSubmitted = false;
      }
    });
  }

  onResetSubmit(): void {
    this.resetSubmitted = true;
    this.success = null;

    // Stop here if form is invalid
    if (this.resetForm.invalid || !this.token) {
      return;
    }

    this.authContext.resetPassword(this.token, this.f['password'].value).subscribe(success => {
      if (success) {
        this.success = 'Your password has been reset successfully.';
        setTimeout(() => {
          this.router.navigate(['/auth/login']);
        }, 3000);
      }
    });
  }
}
