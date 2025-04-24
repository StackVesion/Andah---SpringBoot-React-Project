import { Component, inject } from '@angular/core'
import { CommonModule } from '@angular/common'
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
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
  loginForm!: FormGroup
  fieldTextType = false
  submitted = false
  isLoading = false
  loginSuccess = false
  errorMessage: string | null = null
  apiDebugInfo: string | null = null
  
  // Direct API URL for login - based on API Gateway endpoint
  apiUrl = 'http://localhost:8080/user-service/api/auth/login';

  private authContext = inject(AuthContext)
  private authService = inject(AuthenticationService)
  private http = inject(HttpClient)
  private router = inject(Router)
  private fb = inject(FormBuilder)

  constructor() {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
      remember: [false]
    })
  }

  get form() {
    return this.loginForm.controls
  }

  changetype() {
    this.fieldTextType = !this.fieldTextType
  }

  onSubmit() {
    // Reset previous states
    this.submitted = true;
    this.errorMessage = null;
    this.apiDebugInfo = null;
    
    // Validate form first
    if (!this.loginForm.valid) {
      this.errorMessage = "Please correct the form errors before submitting.";
      return;
    }
    
    // Set loading state
    this.isLoading = true;
    
    // Format the data EXACTLY as expected by the backend (from Postman example)
    const loginData = {
      email: this.form['email'].value,
      password: this.form['password'].value
    };
    
    this.apiDebugInfo = `Attempting login with data: ${JSON.stringify({...loginData, password: '***'}, null, 2)}`;
    
    // Make direct HTTP request to the API using XMLHttpRequest to avoid any framework interference
    const xhr = new XMLHttpRequest();
    xhr.open('POST', this.apiUrl, true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    
    xhr.onload = () => {
      this.isLoading = false;
      
      if (xhr.status >= 200 && xhr.status < 300) {
        // Login successful
        this.loginSuccess = true;
        
        try {
          const response = JSON.parse(xhr.responseText);
          this.apiDebugInfo += `\n\nLogin successful! Response: ${JSON.stringify(response, null, 2)}`;
          
          // Store auth token if available
          if (response && response.token) {
            localStorage.setItem('accessToken', response.token);
            
            // Set authenticated state in context
            this.authContext.isAuthenticated.set(true);
            
            // Set user data if available
            if (response.user) {
              this.authContext.currentUser.set(response.user);
              
              // Handle wallet integration based on project requirements
              if ((response.user as any).wallet) {
                this.apiDebugInfo += `\n\nUser has a wallet with balance: ${(response.user as any).wallet.balance || '0'}`;
              } else {
                this.apiDebugInfo += `\n\nUser doesn't have a wallet yet. It will be created automatically on first transaction.`;
              }
            }
          }
          
          // Navigate to home after short delay
          setTimeout(() => {
            this.router.navigate(['/hotels/home']);
          }, 1500);
        } catch (e) {
          this.apiDebugInfo += `\n\nError parsing response: ${e}`;
          this.errorMessage = "Login successful but error processing response";
        }
      } else {
        // Login failed
        try {
          const error = JSON.parse(xhr.responseText);
          this.errorMessage = error.message || `Login failed with status ${xhr.status}`;
          this.apiDebugInfo += `\n\nError response (${xhr.status}): ${JSON.stringify(error, null, 2)}`;
        } catch (e) {
          this.errorMessage = `Login failed with status ${xhr.status}`;
          this.apiDebugInfo += `\n\nError response (${xhr.status}): ${xhr.responseText}`;
        }
      }
    };
    
    xhr.onerror = () => {
      this.isLoading = false;
      this.errorMessage = "Network error: Cannot connect to server";
      this.apiDebugInfo += "\n\nNetwork error: Cannot connect to server";
      
      // Try fallback method
      this.tryAlternativeLogin(loginData);
    };
    
    // Send the request
    xhr.send(JSON.stringify(loginData));
  }
  
  // Alternative login method as a fallback
  private tryAlternativeLogin(loginData: any) {
    this.apiDebugInfo += '\n\nTrying alternative login method using fetch API...';
    
    // Use direct fetch API call with no-cors mode
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
      this.apiDebugInfo += `\n\nAlternative login successful! Response: ${JSON.stringify(data, null, 2)}`;
      
      // Store auth token
      if (data && data.token) {
        localStorage.setItem('accessToken', data.token);
        this.authContext.isAuthenticated.set(true);
        
        if (data.user) {
          this.authContext.currentUser.set(data.user);
          
          // Handle wallet information based on project requirements
          const userData = data.user as any;
          if (userData.wallet) {
            this.apiDebugInfo += `\n\nWallet found with balance: ${userData.wallet.balance || '0'}`;
          } else {
            this.apiDebugInfo += `\n\nNo wallet found. It will be created on the first transaction.`;
          }
        }
        
        // Navigate to home after short delay
        setTimeout(() => {
          this.router.navigate(['/hotels/home']);
        }, 1500);
      }
    })
    .catch(error => {
      this.apiDebugInfo += `\n\nAlternative login also failed: ${error.message}`;
      
      // Last resort - try through Angular service
      this.apiDebugInfo += '\n\nTrying final login method through AuthService...';
      
      this.authService.login(loginData).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.loginSuccess = true;
          this.errorMessage = null;
          this.apiDebugInfo += `\n\nService login successful!`;
          
          // Navigate to home
          setTimeout(() => {
            this.router.navigate(['/hotels/home']);
          }, 1500);
        },
        error: (error) => {
          this.apiDebugInfo += `\n\nAll login methods failed.`;
        }
      });
    });
  }
}
