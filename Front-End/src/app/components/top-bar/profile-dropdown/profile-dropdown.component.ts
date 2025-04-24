import { ThemeModeService } from '@/app/core/services/theme-mode.service'
import { Component, inject, Input, Renderer2 } from '@angular/core'
import { RouterLink } from '@angular/router'
import { NgbDropdownModule, NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap'
import { AuthContext } from '@/app/core/contexts/auth.context'
import { CommonModule } from '@angular/common'

@Component({
  selector: 'app-profile-dropdown',
  standalone: true,
  imports: [NgbDropdownModule, NgbTooltipModule, RouterLink, CommonModule],
  templateUrl: './profile-dropdown.component.html',
  styles: ``,
})
export class ProfileDropdownComponent {
  @Input() showThemeToggler: boolean = true
  @Input() className: string = ''

  theme: string = 'light'

  public themeModeService = inject(ThemeModeService)
  public authContext = inject(AuthContext)

  changeTheme(mode: 'light' | 'dark' | 'auto') {
    this.theme = mode
    this.themeModeService.updateTheme(mode)
  }

  logout() {
    this.authContext.logout();
  }
}
