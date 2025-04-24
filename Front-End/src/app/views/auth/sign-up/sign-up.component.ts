import { credits, currentYear } from '@/app/store'
import { CommonModule } from '@angular/common'
import { Component, inject } from '@angular/core'
import {
  FormsModule,
  ReactiveFormsModule,
  UntypedFormBuilder,
  Validators,
  type AbstractControl,
  type UntypedFormGroup,
} from '@angular/forms'
import { Router, RouterModule } from '@angular/router'
import { AuthContext } from '@/app/core/contexts/auth.context'
import { AuthenticationService } from '@/app/core/services/auth.service'
import { HttpClient, HttpHeaders } from '@angular/common/http'

@Component({
  selector: 'auth-sign-up',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './sign-up.component.html',
  styles: `
    :host(auth-sign-up) {
      display: contents;
    }
  `,
})
export class SignUpComponent {
  creditsBy = credits
  currentYear = currentYear
  fieldTextType: boolean = false
  fieldTextType1: boolean = false
  signupForm!: UntypedFormGroup
  submitted: boolean = false
  isLoading: boolean = false
  errorMessage: string | null = null
  registrationSuccess: boolean = false
  apiDebugInfo: string | null = null
  
  // The API URL confirmed working in Postman
  apiUrl = 'http://localhost:8080/user-service/api/auth/register'

  public fb = inject(UntypedFormBuilder)
  private authContext = inject(AuthContext)
  private authService = inject(AuthenticationService)
  private http = inject(HttpClient)
  private router = inject(Router)

  constructor() {
    this.signupForm = this.fb.group(
      {
        firstName: ['', [Validators.required]],
        lastName: ['', [Validators.required]],
        email: ['', [Validators.required, Validators.email]],
        phoneNumber: ['', [Validators.required, Validators.pattern(/^\+?[0-9\s-()]{8,}$/)]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmpwd: ['', [Validators.required]],
      },
      { validators: this.validateAreEqual }
    )
  }

  public validateAreEqual(c: AbstractControl): { notSame: boolean } | null {
    return c.value.password === c.value.confirmpwd ? null : { notSame: true }
  }

  changetype() {
    this.fieldTextType = !this.fieldTextType
  }

  get form() {
    return this.signupForm.controls
  }

  onSubmit() {
    this.submitted = true
    this.errorMessage = null
    this.apiDebugInfo = null
    
    if (this.signupForm.valid) {
      this.isLoading = true
      
      // Format the data EXACTLY as expected by the backend (from Postman example)
      // Note: Backend expects 'firstname' but our interface uses 'firstName'
      const registerData = {
        name: `${this.form['firstName'].value} ${this.form['lastName'].value}`,
        firstName: this.form['firstName'].value, // Corrected to match interface
        lastName: this.form['lastName'].value,
        email: this.form['email'].value,
        password: this.form['password'].value,
        phoneNumber: this.form['phoneNumber'].value
      }
      
      this.apiDebugInfo = `Attempting registration with AuthService...`;
      
      // Use the AuthService instead of direct API calls
      this.authService.register(registerData).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.registrationSuccess = true;
          this.apiDebugInfo += `\n\nRegistration successful! Response: ${JSON.stringify(response, null, 2)}`;
          
          // Check for wallet information
          if (response.user) {
            const userData = response.user as any; // Cast to allow wallet access
            if (!userData.wallet) {
              this.apiDebugInfo += `\n\nNo wallet found for user. Will be initialized on first transaction.`;
            } else {
              this.apiDebugInfo += `\n\nWallet found with balance: ${userData.wallet.balance || '0'}`;
            }
          }
          
          // Navigate after a short delay
          setTimeout(() => {
            this.router.navigate(['/hotels/home']);
          }, 1500);
        },
        error: (error) => {
          this.isLoading = false;
          
          if (error.status === 0) {
            this.errorMessage = 'Network error: Cannot connect to server. Is the API Gateway running?';
          } else if (error.status === 409) {
            this.errorMessage = 'This email is already registered. Please use a different email.';
          } else if (error.status === 400) {
            this.errorMessage = `Invalid data: ${error.error?.message || 'Please check all fields'}`;
          } else {
            this.errorMessage = `Registration failed: ${error.message || error.statusText || 'Unknown error'}`;
          }
          
          this.apiDebugInfo += `\n\nError: ${JSON.stringify(error, null, 2)}`;
          console.error('Registration error:', error);
        }
      });
    }
  }
}
