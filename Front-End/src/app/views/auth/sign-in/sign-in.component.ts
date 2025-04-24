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
import { HttpClient, HttpHeaders } from '@angular/common/http'

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
  
  // Direct API URL for login
  apiUrl = 'http://localhost:8080/user-service/api/auth/login';

  public fb = inject(UntypedFormBuilder)
  private authContext = inject(AuthContext)
  private authService = inject(AuthenticationService)
  private http = inject(HttpClient)
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
    // Prevent default form behavior
    this.submitted = true
    this.errorMessage = null
    this.debugInfo = null
    
    if (!this.loginForm.valid) {
      this.errorMessage = "Please correct the form errors before submitting.";
      return;
    }
    
    this.isLoading = true;
    
    // Format the data as expected by the API
    const loginData = {
      email: this.form['email'].value,
      password: this.form['password'].value
    };
    
    this.debugInfo = `Attempting login with data: ${JSON.stringify({...loginData, password: '***'}, null, 2)}`;
    
    // Make direct HTTP request to the API
    this.http.post(this.apiUrl, loginData, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        this.loginSuccess = true;
        this.debugInfo = `Login successful! Response: ${JSON.stringify(response, null, 2)}`;
        
        // Store auth token if available
        if (response && response.token) {
          localStorage.setItem('accessToken', response.token);
          
          // Set authenticated state in context
          this.authContext.isAuthenticated.set(true);
          
          // Set user data if available
          if (response.user) {
            this.authContext.currentUser.set(response.user);
            
            // Check for wallet information (based on project requirements)
            const userData = response.user as any;
            if (userData.wallet) {
              this.debugInfo += `\n\nWallet found with balance: ${userData.wallet.balance || '0'}`;
            } else {
              this.debugInfo += `\n\nNo wallet found. Wallet will be initialized on first transaction.`;
            }
          }
          
          // Delay navigation to show success message
          setTimeout(() => {
            this.router.navigate(['/hotels/home']);
          }, 1500);
        }
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Login error:', error);
        
        if (error.status === 0) {
          this.errorMessage = "Network error: Cannot connect to server";
          this.debugInfo = "Network error: Cannot connect to server. Check if the API Gateway is running.";
        } else if (error.status === 401 || error.status === 403) {
          this.errorMessage = "Invalid email or password";
          this.debugInfo = `Error ${error.status}: Invalid credentials`;
        } else if (error.error && error.error.message) {
          this.errorMessage = error.error.message;
          this.debugInfo = `Error ${error.status}: ${error.error.message}`;
        } else {
          this.errorMessage = `Login failed (${error.status || 'unknown'})`;
          this.debugInfo = `Error ${error.status || 'unknown'}: ${error.message || 'Unknown error'}`;
        }
        
        // Try alternative login as a fallback
        this.tryAlternativeLogin(loginData);
      }
    });
  }
  
  // Alternative login method as a fallback
  private tryAlternativeLogin(loginData: any) {
    this.debugInfo += '\n\nTrying alternative login method...';
    
    // Use direct fetch API call
    fetch(this.apiUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(loginData)
    })
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
      return response.json();
    })
    .then(data => {
      this.isLoading = false;
      this.loginSuccess = true;
      this.errorMessage = null;
      this.debugInfo += `\n\nAlternative login successful! Response: ${JSON.stringify(data, null, 2)}`;
      
      // Store auth token
      if (data && data.token) {
        localStorage.setItem('accessToken', data.token);
        this.authContext.isAuthenticated.set(true);
        
        if (data.user) {
          this.authContext.currentUser.set(data.user);
          
          // Check for wallet info
          const userData = data.user as any;
          if (userData.wallet) {
            this.debugInfo += `\n\nWallet found with balance: ${userData.wallet.balance || '0'}`;
          }
        }
        
        // Navigate to home
        setTimeout(() => {
          this.router.navigate(['/hotels/home']);
        }, 1500);
      }
    })
    .catch(error => {
      this.debugInfo += `\n\nAlternative login also failed: ${error.message}`;
    });
  }
}
