import { credits, currentYear } from '@/app/store'
import { CommonModule } from '@angular/common'
import { Component, inject, ElementRef, ViewChild } from '@angular/core'
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
  
  // Direct API URL for registration - based on working Postman example
  apiUrl = 'http://localhost:8080/user-service/api/auth/register';

  public fb = inject(UntypedFormBuilder)
  private authContext = inject(AuthContext)
  private authService = inject(AuthenticationService)
  private http = inject(HttpClient)
  private router = inject(Router)

  constructor() {
    // Configure form with validators
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
    // Reset previous states
    this.submitted = true;
    this.errorMessage = null;
    this.apiDebugInfo = null;
    
    // Validate form first
    if (!this.signupForm.valid) {
      this.errorMessage = "Please correct the form errors before submitting.";
      return;
    }
    
    // Set loading state
    this.isLoading = true;
    
    // Format the data EXACTLY as expected by the backend (from Postman example)
    const registerData = {
      name: `${this.form['firstName'].value} ${this.form['lastName'].value}`,
      firstName: this.form['firstName'].value,
      lastName: this.form['lastName'].value,
      email: this.form['email'].value,
      password: this.form['password'].value,
      phoneNumber: this.form['phoneNumber'].value
    };
    
    this.apiDebugInfo = `Attempting registration with data: ${JSON.stringify({...registerData, password: '***'}, null, 2)}`;
    
    // Make direct HTTP request to the API using XMLHttpRequest to avoid any framework interference
    const xhr = new XMLHttpRequest();
    xhr.open('POST', this.apiUrl, true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    
    xhr.onload = () => {
      this.isLoading = false;
      
      if (xhr.status >= 200 && xhr.status < 300) {
        // Registration successful
        this.registrationSuccess = true;
        
        try {
          const response = JSON.parse(xhr.responseText);
          this.apiDebugInfo += `\n\nRegistration successful! Response: ${JSON.stringify(response, null, 2)}`;
          
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
          this.errorMessage = "Registration successful but error processing response";
        }
      } else {
        // Registration failed
        try {
          const error = JSON.parse(xhr.responseText);
          this.errorMessage = error.message || `Registration failed with status ${xhr.status}`;
          this.apiDebugInfo += `\n\nError response (${xhr.status}): ${JSON.stringify(error, null, 2)}`;
        } catch (e) {
          this.errorMessage = `Registration failed with status ${xhr.status}`;
          this.apiDebugInfo += `\n\nError response (${xhr.status}): ${xhr.responseText}`;
        }
      }
    };
    
    xhr.onerror = () => {
      this.isLoading = false;
      this.errorMessage = "Network error: Cannot connect to server";
      this.apiDebugInfo += "\n\nNetwork error: Cannot connect to server";
      
      // Try fallback method
      this.tryAlternativeRegistration(registerData);
    };
    
    // Send the request
    xhr.send(JSON.stringify(registerData));
  }
  
  // Alternative registration method as a fallback
  private tryAlternativeRegistration(registerData: any) {
    this.apiDebugInfo += '\n\nTrying alternative registration method using fetch API...';
    
    // Use direct fetch API call with no-cors mode
    fetch(this.apiUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(registerData)
    })
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
      return response.json();
    })
    .then(data => {
      this.isLoading = false;
      this.registrationSuccess = true;
      this.errorMessage = null;
      this.apiDebugInfo += `\n\nAlternative registration successful! Response: ${JSON.stringify(data, null, 2)}`;
      
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
      this.apiDebugInfo += `\n\nAlternative registration also failed: ${error.message}`;
      
      // Last resort - try through Angular service
      this.apiDebugInfo += '\n\nTrying final registration method through AuthService...';
      
      this.authService.register(registerData).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.registrationSuccess = true;
          this.errorMessage = null;
          this.apiDebugInfo += `\n\nService registration successful!`;
          
          // Navigate to home
          setTimeout(() => {
            this.router.navigate(['/hotels/home']);
          }, 1500);
        },
        error: (error) => {
          this.apiDebugInfo += `\n\nAll registration methods failed.`;
        }
      });
    });
  }
}
