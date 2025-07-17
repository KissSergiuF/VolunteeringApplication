import { Component, Inject, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { UserService } from './services/user.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: false,
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Frontend';
  isLoggedIn = false;
  profilePictureUrl: string | null = null;

  // Constructorul injectează serviciile necesare și identifică platforma
  constructor(
    private router: Router,
    private userService: UserService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  // Inițializează componenta și setează ascultător pentru actualizarea pozei de profil
  ngOnInit() {
    this.updateLoginStatus();

    if (isPlatformBrowser(this.platformId)) {
      window.addEventListener('profilePictureUpdated', () => {
        this.updateLoginStatus();
      });
    }
  }

  // Setează URL-ul pentru poza de profil în funcție de formatul primit
  setProfilePicture(profilePic: string | null) {
    if (profilePic && profilePic !== 'null') {
      if (profilePic.startsWith('/uploads')) {
        this.profilePictureUrl = `http://localhost:8080${profilePic}`;
      } else if (profilePic.startsWith('http')) {
        this.profilePictureUrl = profilePic;
      } else {
        this.profilePictureUrl = `/${profilePic}`;
      }
    } else {
      this.profilePictureUrl = null;
    }
  }

  // Verifică dacă utilizatorul este logat și încarcă poza de profil
  updateLoginStatus() {
    if (isPlatformBrowser(this.platformId)) {
      const userId = sessionStorage.getItem('userId');
      this.isLoggedIn = !!userId;

      if (this.isLoggedIn) {
        this.userService.getProfile().subscribe({
          next: user => {
            const profile = user.profilePicture;
            if (profile && profile !== 'null') {
              this.setProfilePicture(profile + '?v=' + new Date().getTime());
            } else {
              this.setProfilePicture(null);
            }
          },
          error: () => {
            this.setProfilePicture(null);
          }
        });
      } else {
        this.setProfilePicture(null);
      }
    }
  }

  // Navighează către pagina de login sau înregistrare, în funcție de opțiunea aleasă
  goToLogin(option: 'login' | 'voluntar' | 'organizatie') {
    let state: any = {};

    if (option === 'voluntar') {
      state = { form: 'register', role: 'USER' };
    } else if (option === 'organizatie') {
      state = { form: 'register', role: 'ASSOCIATION' };
    } else {
      state = { form: 'login' };
    }

    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
      this.router.navigate(['/login'], { state });
    });
  }

  // Deloghează utilizatorul și redirecționează către pagina de start
  logout() {
    if (isPlatformBrowser(this.platformId)) {
      sessionStorage.clear();
    }
    this.updateLoginStatus();
    this.router.navigate(['/home']).then(() => {
      window.location.reload();
    });
  }
}
