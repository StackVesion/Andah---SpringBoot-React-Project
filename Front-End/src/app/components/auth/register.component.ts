import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

import { AuthContext } from '../../core/contexts/auth.context';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <div class="register-container">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">Create an Account</h3>
        </div>
        <div class="card-body">
          <div *ngIf="authContext.error()" class="alert alert-danger">
            {{ authContext.error() }}
            <button type="button" class="close" (click)="authContext.clearError()">
              <span>&times;</span>
            </button>
          </div>

          <form [formGroup]="registerForm" (ngSubmit)="onSubmit()">
            <div class="row">
              <div class="col-md-6">
                <div class="form-group">
                  <label for="firstName">First Name</label>
                  <input 
                    type="text" 
                    class="form-control" 
                    id="firstName" 
                    formControlName="firstName" 
                    placeholder="Enter your first name"
                    [ngClass]="{'is-invalid': submitted && f['firstName'].errors}"
                  >
                  <div *ngIf="submitted && f['firstName'].errors" class="invalid-feedback">
                    <div *ngIf="f['firstName'].errors['required']">First name is required</div>
                  </div>
                </div>
              </div>
              <div class="col-md-6">
                <div class="form-group">
                  <label for="lastName">Last Name</label>
                  <input 
                    type="text" 
                    class="form-control" 
                    id="lastName" 
                    formControlName="lastName" 
                    placeholder="Enter your last name"
                    [ngClass]="{'is-invalid': submitted && f['lastName'].errors}"
                  >
                  <div *ngIf="submitted && f['lastName'].errors" class="invalid-feedback">
                    <div *ngIf="f['lastName'].errors['required']">Last name is required</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="form-group">
              <label for="name">Username</label>
              <input 
                type="text" 
                class="form-control" 
                id="name" 
                formControlName="name" 
                placeholder="Enter your username"
                [ngClass]="{'is-invalid': submitted && f['name'].errors}"
              >
              <div *ngIf="submitted && f['name'].errors" class="invalid-feedback">
                <div *ngIf="f['name'].errors['required']">Username is required</div>
              </div>
            </div>

            <div class="form-group">
              <label for="email">Email</label>
              <input 
                type="email" 
                class="form-control" 
                id="email" 
                formControlName="email" 
                placeholder="Enter your email"
                [ngClass]="{'is-invalid': submitted && f['email'].errors}"
              >
              <div *ngIf="submitted && f['email'].errors" class="invalid-feedback">
                <div *ngIf="f['email'].errors['required']">Email is required</div>
                <div *ngIf="f['email'].errors['email']">Please enter a valid email</div>
              </div>
            </div>

            <div class="form-group">
              <label for="phoneNumber">Phone Number</label>
              <input 
                type="tel" 
                class="form-control" 
                id="phoneNumber" 
                formControlName="phoneNumber" 
                placeholder="Enter your phone number"
                [ngClass]="{'is-invalid': submitted && f['phoneNumber'].errors}"
              >
              <div *ngIf="submitted && f['phoneNumber'].errors" class="invalid-feedback">
                <div *ngIf="f['phoneNumber'].errors['required']">Phone number is required</div>
                <div *ngIf="f['phoneNumber'].errors['pattern']">Please enter a valid phone number</div>
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
                [ngClass]="{'is-invalid': submitted && f['password'].errors}"
              >
              <div *ngIf="submitted && f['password'].errors" class="invalid-feedback">
                <div *ngIf="f['password'].errors['required']">Password is required</div>
                <div *ngIf="f['password'].errors['minlength']">Password must be at least 6 characters</div>
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
                [ngClass]="{'is-invalid': submitted && f['confirmPassword'].errors}"
              >
              <div *ngIf="submitted && f['confirmPassword'].errors" class="invalid-feedback">
                <div *ngIf="f['confirmPassword'].errors['required']">Password confirmation is required</div>
                <div *ngIf="f['confirmPassword'].errors['mustMatch']">Passwords must match</div>
              </div>
            </div>

            <div class="form-group form-check">
              <input 
                type="checkbox" 
                class="form-check-input" 
                id="termsAccepted" 
                formControlName="termsAccepted"
                [ngClass]="{'is-invalid': submitted && f['termsAccepted'].errors}"
              >
              <label class="form-check-label" for="termsAccepted">
                I accept the <a href="#" target="_blank">Terms of Service</a> and <a href="#" target="_blank">Privacy Policy</a>
              </label>
              <div *ngIf="submitted && f['termsAccepted'].errors" class="invalid-feedback">
                <div *ngIf="f['termsAccepted'].errors['required']">You must accept the terms to continue</div>
              </div>
            </div>

            <button 
              type="submit" 
              class="btn btn-primary btn-block" 
              [disabled]="authContext.isLoading()"
            >
              <span *ngIf="authContext.isLoading()" class="spinner-border spinner-border-sm mr-1"></span>
              Register
            </button>
          </form>

          <div class="alternative-actions mt-3">
            <p>
              Already have an account? <a routerLink="/login">Login</a>
            </p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .register-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 80vh;
      padding: 20px;
    }
    .card {
      width: 100%;
      max-width: 600px;
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
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  submitted = false;

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

    this.registerForm = this.formBuilder.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^\+?[0-9]{8,15}$/)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      termsAccepted: [false, Validators.requiredTrue]
    }, {
      validator: this.passwordMatchValidator
    });
  }

  // Convenience getter for easy access to form fields
  get f() { return this.registerForm.controls; }

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

  onSubmit(): void {
    this.submitted = true;

    // Stop here if form is invalid
    if (this.registerForm.invalid) {
      return;
    }

    const registerData = {
      firstName: this.f['firstName'].value,
      lastName: this.f['lastName'].value,
      email: this.f['email'].value,
      password: this.f['password'].value,
      phoneNumber: this.f['phoneNumber'].value
    };

    this.authContext.register(registerData).subscribe(success => {
      if (success) {
        // Show success message and redirect to login or home
        alert('Registration successful!');
        this.router.navigate(['/']);
      }
    });
  }
}
