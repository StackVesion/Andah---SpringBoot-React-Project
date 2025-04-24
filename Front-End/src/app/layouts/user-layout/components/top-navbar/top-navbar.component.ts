import { Component, inject, OnInit, effect, signal } from '@angular/core'
import { Router } from '@angular/router'
import { AuthContext } from '@/app/core/contexts/auth.context'
import { User } from '@/app/core/models/auth.model'
import Swal from 'sweetalert2'
import { AppMenuComponent } from '@/app/components/app-menu/app-menu.component'
import { VerticalMenuButtonComponent } from '@/app/components/app-menu/components/vertical-menu-button.component'
import { LogoBoxComponent } from '@/app/components/logo-box/logo-box.component'
import { StickyHeaderComponent } from '@/app/components/sticky-header.component'
import { NotificationDropdownComponent } from '@/app/components/top-bar/notification-dropdown/notification-dropdown.component'
import { ProfileDropdownComponent } from '@/app/components/top-bar/profile-dropdown/profile-dropdown.component'
import { NgbCollapseModule, NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap'
import { RouterLink } from '@angular/router'
import { CommonModule } from '@angular/common'

@Component({
  selector: 'user-top-navbar',
  standalone: true,
  imports: [
    AppMenuComponent,
    NgbDropdownModule,
    LogoBoxComponent,
    NgbCollapseModule,
    VerticalMenuButtonComponent,
    NotificationDropdownComponent,
    ProfileDropdownComponent,
    StickyHeaderComponent,
    RouterLink,
    CommonModule,
  ],
  template: `
    <header class="navbar-light navbar-sticky header-static">
      <nav class="navbar navbar-expand-xl">
        <div class="container-fluid px-3 px-xl-5">
          <a class="navbar-brand" routerLink="/">
            <img class="light-mode-item navbar-brand-item" src="assets/images/logo.svg" alt="logo" />
            <img class="dark-mode-item navbar-brand-item" src="assets/images/logo-light.svg" alt="logo" />
          </a>

          <div class="navbar-collapse collapse show" [ngbCollapse]="!isMobileMenuOpen" id="navbarCollapse">
            <ul class="navbar-nav mx-auto">
              <!-- Nav item HOME -->
              <li class="nav-item">
                <a class="nav-link" routerLink="/hotels/home">
                  <i class="bi bi-house-door me-1"></i>Home
                </a>
              </li>
              
              <!-- Add wallet link if user is authenticated -->
              <li class="nav-item" *ngIf="isAuthenticated()">
                <a class="nav-link" routerLink="/wallet">
                  <i class="bi bi-wallet2 me-1"></i>Wallet
                  <span *ngIf="walletBalance() !== null" class="badge bg-success ms-2">{{walletBalance()}}</span>
                </a>
              </li>

            </ul>
          </div>

          <vertical-menu-button className="ms-auto mx-3 me-md-0" [showText]="false" (click)="toggleMobileMenu()" />
          <app-menu [showContactPages]="true" />

          <ul class="nav flex-row align-items-center list-unstyled ms-xl-auto">
            <app-notification-dropdown className="ms-2" />

            <!-- Profile dropdown - only show if authenticated -->
            <li *ngIf="isAuthenticated()">
              <app-profile-dropdown className="ms-3" />
              
              <!-- Show user name if available -->
              <span class="d-none d-lg-inline-block ms-2 me-2 text-primary">
                Welcome, {{userDisplayName}}!
              </span>
            </li>

            <!-- Register button - only show if not authenticated -->
            <li class="nav-item ms-3 d-none d-sm-block" *ngIf="!isAuthenticated()">
              <a
                class="btn btn-sm btn-outline-primary mb-0"
                routerLink="/auth/sign-up"
                ><i class="bi bi-person-plus me-1"></i> Register</a
              >
            </li>

            <!-- Logout button - only show if authenticated -->
            <li class="nav-item ms-3 d-none d-sm-block" *ngIf="isAuthenticated()">
              <a
                class="btn btn-sm btn-outline-danger mb-0"
                (click)="logout()"
                ><i class="bi bi-box-arrow-right me-1"></i> Logout</a
              >
            </li>

            <!-- Login button - only show if not authenticated -->
            <li class="nav-item ms-3 d-none d-sm-block" *ngIf="!isAuthenticated()">
              <a
                class="btn btn-sm btn-primary mb-0"
                routerLink="/auth/sign-in"
                ><i class="bi bi-box-arrow-in-right me-1"></i> Login</a
              >
            </li>
          </ul>
        </div>
      </nav>
    </header>
  `,
  styles: `
    .nav-link:hover {
      color: var(--bs-primary);
    }
    
    a {
      cursor: pointer;
    }
  `,
})
export class TopNavbarComponent implements OnInit {
  currentUser = signal<User | null>(null);
  isAuthenticated = signal<boolean>(false);
  
  // Mobile menu state
  isMobileMenuOpen = false;
  
  private authContext = inject(AuthContext);
  private router = inject(Router);
  
  constructor() {
    // Create an effect to update local signals when auth state changes
    effect(() => {
      this.currentUser.set(this.authContext.currentUser());
      this.isAuthenticated.set(this.authContext.isAuthenticated());
    });
  }
  
  ngOnInit() {
    // Initial state setup
    this.currentUser.set(this.authContext.currentUser());
    this.isAuthenticated.set(this.authContext.isAuthenticated());
  }
  
  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }
  
  login() {
    this.router.navigate(['/auth/sign-in']);
  }
  
  register() {
    this.router.navigate(['/auth/sign-up']);
  }
  
  logout() {
    // Confirm logout
    Swal.fire({
      title: 'Logout',
      text: 'Are you sure you want to log out?',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Yes, log out',
      cancelButtonText: 'Cancel'
    }).then((result) => {
      if (result.isConfirmed) {
        // Call logout method from AuthContext
        this.authContext.logout();
        
        // Show logout success message
        Swal.fire({
          title: 'Logged Out',
          text: 'You have been successfully logged out',
          icon: 'success',
          timer: 2000,
          showConfirmButton: false
        });
      }
    });
  }
  
  // Get user display name
  get userDisplayName(): string {
    const user = this.currentUser();
    if (user) {
      if (user.firstName) {
        return user.firstName;
      } else if ((user as any).name) {
        return (user as any).name.split(' ')[0]; // Get first name
      } else if (user.email) {
        return user.email.split('@')[0]; // Get username part of email
      }
    }
    return 'User';
  }
  
  // Get wallet balance if available
  get walletBalance(): string | null {
    const user = this.currentUser();
    if (user && (user as any).wallet) {
      return (user as any).wallet.balance || '0';
    }
    return null;
  }
}
