import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user.service';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Feedback } from '../../models/feedback.model';

@Component({
  selector: 'app-profil',
  templateUrl: './profil.component.html',
  standalone: false,
  styleUrls: ['./profil.component.css']
})
export class ProfilComponent implements OnInit {
  user: any = null;
  errorMessage: string = '';
  editing: boolean = false;
  feedbackList: Feedback[] = [];
  showFeedbacks: boolean = false;
  averageRating: number = 0;
  feedbackCount: number = 0;

  editData: any = {
    firstName: '',
    lastName: '',
    telephone: ''
  };

  selectedFile: File | null = null;
  previewUrl: string | ArrayBuffer | null = null;

  constructor(private userService: UserService, private router: Router, private snackBar: MatSnackBar) {}

  /** Inițializează componenta și încarcă datele utilizatorului */
  ngOnInit(): void {
    this.loadUserProfile();
  }

  /** Încarcă datele utilizatorului logat */
  loadUserProfile(): void {
    this.userService.getProfile().subscribe({
      next: (data) => {
        this.user = data;

        if (this.user.profilePicture) {
          this.user.profilePicture += '?v=' + new Date().getTime();
        }

        this.editData = {
          firstName: data.firstName,
          lastName: data.lastName,
          telephone: data.telephone
        };

        this.editing = false;
        this.selectedFile = null;
        this.previewUrl = null;
        this.loadAverageRating(); 
      },
      error: (err) => {
        this.errorMessage = err.error?.message || err.error || 'Eroare la preluarea datelor.';
        this.snackBar.open(this.errorMessage, 'Închide', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom',
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  /** Activează modul de editare a profilului */
  enableEdit(): void {
    this.editing = true;
    this.editData = {
      firstName: this.user.firstName,
      lastName: this.user.lastName,
      telephone: this.user.telephone
    };
    this.selectedFile = null;
    this.previewUrl = null;
  }

  /** Anulează modificările și revine la vizualizare */
  cancelEdit(): void {
    this.editing = false;
    this.selectedFile = null;
    this.previewUrl = null;
  }

  /** Gestionează fișierul selectat pentru poza de profil */
  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = () => {
        this.previewUrl = reader.result;
      };
      reader.readAsDataURL(file);
    }
  }

  /** Trimite modificările profilului către backend */
  submitProfileEdit(): void {
    const formData = new FormData();
    formData.append('firstName', this.editData.firstName);
    formData.append('lastName', this.editData.lastName);
    formData.append('telephone', this.editData.telephone);

    if (this.selectedFile) {
      formData.append('profilePicture', this.selectedFile);
    }

    this.userService.updateProfile(formData).subscribe({
      next: (response) => {
        this.editing = false;
        this.loadUserProfile();
        
        setTimeout(() => {
          window.dispatchEvent(new CustomEvent('profilePictureUpdated'));
        }, 100);
        
        this.snackBar.open('Profilul a fost actualizat cu succes!', 'Închide', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom',
          panelClass: ['success-snackbar']
        });
        
        this.selectedFile = null;
        this.previewUrl = null;
      },
      error: (err) => {
        const errorMessage = typeof err.error === 'string' ? err.error : 'Eroare la actualizarea profilului.';
        this.errorMessage = errorMessage;
        this.snackBar.open(errorMessage, 'Închide', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom',
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  /** Încarcă feedback-urile primite de utilizator */
  loadFeedbacks(): void {
    const userId = this.user.id; 
    this.userService.getFeedbackForUser(userId).subscribe({
      next: (feedbacks) => {
        this.feedbackList = feedbacks;
        this.showFeedbacks = true;
      },
      error: (err) => {
        console.error('Eroare la încărcarea feedback-urilor:', err);
      }
    });
  }

  /** Încarcă media ratingului și numărul total de feedback-uri */
  loadAverageRating(): void {
    const userId = this.user.id;
    this.userService.getAverageRatingForUser(userId).subscribe({
      next: (avg) => {
        this.averageRating = avg ?? 0;
        this.userService.getFeedbackCountForUser(userId).subscribe({
          next: (count) => {
            this.feedbackCount = count;
          },
          error: (err) => {
            console.error('Eroare la numărul de evaluări:', err);
          }
        });
      },
      error: (err) => {
        console.error('Eroare la media ratingului:', err);
      }
    });
  }

  /** Comută afișarea feedback-urilor */
  toggleFeedbacks(): void {
    if (this.showFeedbacks) {
      this.showFeedbacks = false;
    } else {
      this.loadFeedbacks();
    }
  }
}
