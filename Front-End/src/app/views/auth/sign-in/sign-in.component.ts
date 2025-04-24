import { credits, currentYear } from '@/app/store'
import { CommonModule } from '@angular/common'
import { Component, inject } from '@angular/core'
import {
  FormsModule,
  ReactiveFormsModule,
  UntypedFormBuilder,
  UntypedFormGroup,
  Validators,
} from '@angular/forms'
import { Router, RouterModule } from '@angular/router'
import { AuthContext } from '@/app/core/contexts/auth.context'
import { AuthenticationService } from '@/app/core/services/auth.service'

@Component({
  selector: 'auth-sign-in',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './sign-in.component.html',
  styles: `
    :host(auth-sign-in) {
      display: contents;
    }
  `,
})
export class SignInComponent {
  creditsBy = credits
  currentYear = currentYear
  fieldTextType: boolean = false
  loginForm!: UntypedFormGroup
  submitted: boolean = false
  isLoading: boolean = false
  loginSuccess: boolean = false
  errorMessage: string | null = null
  debugInfo: string | null = null

  public fb = inject(UntypedFormBuilder)
  private authContext = inject(AuthContext)
  private authService = inject(AuthenticationService)
  private router = inject(Router)

  constructor() {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
    })
  }

  changetype() {
    this.fieldTextType = !this.fieldTextType
  }

  get form() {
    return this.loginForm.controls
  }

  onSubmit() {
    this.submitted = true
    this.errorMessage = null
    this.debugInfo = null
    
    if (this.loginForm.valid) {
      this.isLoading = true
      
      // Format the data as expected by the API
      const loginData = {
        email: this.form['email'].value,
        password: this.form['password'].value
      }
      
      this.debugInfo = `Attempting login with AuthService...`;
      
      // Use the AuthService for login
      this.authService.login(loginData).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.loginSuccess = true;
          this.debugInfo += `\n\nLogin successful! Response: ${JSON.stringify(response, null, 2)}`;
          
          // Check for wallet information
          if (response.user) {
            const userData = response.user as any; // Cast to allow wallet access
            if (!userData.wallet) {
              this.debugInfo += `\n\nNo wallet found for user. Will be initialized on first transaction.`;
            } else {
              this.debugInfo += `\n\nWallet found with balance: ${userData.wallet.balance || '0'}`;
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
          } else if (error.status === 401 || error.status === 403) {
            this.errorMessage = 'Invalid email or password. Please try again.';
          } else {
            this.errorMessage = `Login failed: ${error.message || error.statusText || 'Unknown error'}`;
          }
          
          this.debugInfo += `\n\nError: ${JSON.stringify(error, null, 2)}`;
          console.error('Login error:', error);
        }
      });
    }
  }
}
