import { AppMenuComponent } from '@/app/components/app-menu/app-menu.component'
import { VerticalMenuButtonComponent } from '@/app/components/app-menu/components/vertical-menu-button.component'
import { LogoBoxComponent } from '@/app/components/logo-box/logo-box.component'
import { StickyHeaderComponent } from '@/app/components/sticky-header.component'
import { NotificationDropdownComponent } from '@/app/components/top-bar/notification-dropdown/notification-dropdown.component'
import { ProfileDropdownComponent } from '@/app/components/top-bar/profile-dropdown/profile-dropdown.component'
import { Component, inject } from '@angular/core'
import {
  NgbCollapseModule,
  NgbDropdownModule,
} from '@ng-bootstrap/ng-bootstrap'
import { RouterLink } from '@angular/router'
import { AuthContext } from '@/app/core/contexts/auth.context'
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
    <sticky-header-component class="navbar-light header-sticky">
      <nav class="navbar navbar-expand-xl">
        <div class="container">
          <app-logo-box />

          <!-- Main Navigation Links -->
          <div class="collapse navbar-collapse" id="navbarCollapse">
            <ul class="navbar-nav me-auto">
              <li class="nav-item">
                <a class="nav-link active" routerLink="/hotels/home">
                  <i class="bi bi-house-door me-1"></i>Home
                </a>
              </li>
              
              <!-- Add wallet link if user is authenticated -->
              @if (authContext.isAuthenticated()) {
                <li class="nav-item">
                  <a class="nav-link" routerLink="/wallet">
                    <i class="bi bi-wallet2 me-1"></i>Wallet
                  </a>
                </li>
              }

            </ul>
          </div>

          <vertical-menu-button className="ms-auto mx-3 me-md-0" [showText]="false" />
          <app-menu [showContactPages]="true" />

          <ul class="nav flex-row align-items-center list-unstyled ms-xl-auto">
            <app-notification-dropdown />

            <app-profile-dropdown className="ms-3" />

            @if (!authContext.isAuthenticated()) {
              <li class="nav-item ms-3 d-none d-sm-block">
                <a
                  class="btn btn-sm btn-primary-soft mb-0"
                  routerLink="/auth/register"
                  ><i class="bi bi-person-plus"></i> Register</a
                >
              </li>
            }
          </ul>
        </div>
      </nav>
    </sticky-header-component>
  `,
  styles: ``,
})
export class TopNavbarComponent {
  isMenuOpen: boolean = true
  public authContext = inject(AuthContext)

  toggleMobille(event: any) {
    let ariaExpand = event.target.closest('button.navbar-toggler')

    this.isMenuOpen = !this.isMenuOpen
    if (this.isMenuOpen) {
      ariaExpand.setAttribute('aria-expanded', 'false')
    } else {
      ariaExpand.setAttribute('aria-expanded', 'true')
    }
  }
}
