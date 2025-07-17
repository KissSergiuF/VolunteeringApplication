import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormGroup, FormControl, Validators, FormBuilder } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  
  registerForm: FormGroup;

  showRegister = false;
  userInfo: any;
  errorMessage = '';

  emailPattern = '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$';

  constructor(
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
    private fb: FormBuilder,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    // Initializare formular login cu validări
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.pattern(this.emailPattern)]],
      password: ['', Validators.required]
    });

    // Initializare formular register cu validări
    this.registerForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.pattern(this.emailPattern)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      telephone: ['', Validators.pattern('^((\\+[\\d]{2,4}\\d{9})|(0\\d{9}))$')],
      role: ['USER', Validators.required]
    });

    // Verifică dacă componenta a fost deschisă cu parametri de navigare
    const navState = this.router.getCurrentNavigation()?.extras.state;

    if (navState) {
      if (navState['form'] === 'register') {
        this.showRegister = true;
      }

      if (navState['role']) {
        this.registerForm.get('role')?.setValue(navState['role']);
      }
    }
  }

  ngOnInit() {
    // Adaugă stiluri pentru snackbar doar în browser
    if (isPlatformBrowser(this.platformId)) {
      const styleTag = document.createElement('style');
      styleTag.textContent = `
        .error-snackbar {
          background-color: #f44336;
          color: white;
        }
        .success-snackbar {
          background-color: #4CAF50;
          color: white;
        }
      `;
      document.head.appendChild(styleTag);
    }
  }

  // Getteri pentru accesarea controlurilor din template
  get loginEmail() { return this.loginForm.get('email'); }
  get loginPassword() { return this.loginForm.get('password'); }
  
  get regFirstName() { return this.registerForm.get('firstName'); }
  get regLastName() { return this.registerForm.get('lastName'); }
  get regEmail() { return this.registerForm.get('email'); }
  get regPassword() { return this.registerForm.get('password'); }
  get regTelephone() { return this.registerForm.get('telephone'); }
  get regRole() { return this.registerForm.get('role'); }

  // Trimite datele de login către backend și gestionează răspunsul
  loginUser() {
    if (this.loginForm.invalid) {
      this.markFormGroupTouched(this.loginForm);
      return;
    }
  
    const formValues = this.loginForm.value;
  
    this.authService.login(formValues.email, formValues.password).subscribe({
      next: (response) => {
        console.log('LOGIN RESPONSE:', response);
  
        // Salvează datele utilizatorului în sessionStorage
        if (isPlatformBrowser(this.platformId)) {
          sessionStorage.setItem('userId', response.userId);
          sessionStorage.setItem('role', response.role);
          sessionStorage.setItem('firstName', response.firstName);
          sessionStorage.setItem('lastName', response.lastName);
          sessionStorage.setItem('profilePicture', response.profilePicture || 'Default_pfp.jpg');
        }
  
        // Redirecționează către pagina Home și reîncarcă pagina
        this.router.navigate(['/home']).then(() => {
          if (isPlatformBrowser(this.platformId)) {
            window.location.reload();
          }
        });
      },
      error: (error) => {
        // Afișează mesaj de eroare pentru login invalid
        this.snackBar.open('Invalid email or password.', 'Close', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top',
          panelClass: ['error-snackbar']
        });
        console.error('Login error:', error);
        this.errorMessage = 'Invalid email or password';
      }
    });
  }

  // Trimite datele de înregistrare către backend și gestionează răspunsul
  registerUser() {
    if (this.registerForm.invalid) {
      this.markFormGroupTouched(this.registerForm);
      return;
    }

    const formValues = this.registerForm.value;
    
    const user = {
      firstName: formValues.firstName,
      lastName: formValues.lastName,
      email: formValues.email,
      password: formValues.password,
      telephone: formValues.telephone,
      role: formValues.role
    };

    this.authService.register(user).subscribe({
      next: (response) => {
        console.log('Register successful:', response);
        this.errorMessage = '';
        this.showRegister = false;

        // Resetează formularele după înregistrarea cu succes
        this.loginForm.reset();
        this.registerForm.reset();
        this.registerForm.get('role')?.setValue('USER');
        
        // Afișează mesaj de succes
        this.snackBar.open('Registration successful! Please log in.', 'Close', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom',
          panelClass: ['success-snackbar']
        });
      },
      error: (error) => {
        console.error('Register error:', error);
        
        // Gestionează eroarea de email deja folosit
        if (error.error && error.error.message === 'Email is already used') {
          this.snackBar.open('This email is already registered. Please try another email address.', 'Close', {
            duration: 5000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['error-snackbar']
          });
          
          this.registerForm.get('email')?.setErrors({ emailInUse: true });
        } else {
          // Afișează alte erori de înregistrare
          this.snackBar.open(error.error?.message || 'Registration failed. Please try again.', 'Close', {
            duration: 3000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['error-snackbar']
          });
          this.errorMessage = error.error?.message || 'Registration failed';
        }
      }
    });
  }

  // Deconectează utilizatorul și șterge datele din sessionStorage
  logoutUser() {
    this.authService.logout().subscribe({
      next: (response) => {
        if (isPlatformBrowser(this.platformId)) {
          sessionStorage.removeItem('userId');
          sessionStorage.removeItem('role');
        }
        this.userInfo = null;
        this.errorMessage = 'Logged out';
      },
      error: (error) => {
        console.error('Logout error:', error);
      }
    });
  }

  // Marchează toate controalele din formular ca fiind atinse pentru afișarea erorilor
  markFormGroupTouched(formGroup: FormGroup) {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  // Comută între formularele de login și register
  toggleRegister(show: boolean) {
    this.showRegister = show;
    this.errorMessage = '';
   
    // Resetează formularul inactiv
    if (show) {
      this.loginForm.reset();
    } else {
      this.registerForm.reset();
      this.registerForm.get('role')?.setValue('USER');
    }
  }
}