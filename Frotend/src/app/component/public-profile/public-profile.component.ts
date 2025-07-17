import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../../services/user.service';
import { Feedback } from '../../models/feedback.model';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-public-profile',
  standalone: false,
  templateUrl: './public-profile.component.html',
  styleUrls: ['./public-profile.component.css']
})
export class PublicProfileComponent implements OnInit {
  userId!: number;
  user: any = null;
  feedbackList: Feedback[] = [];
  averageRating: number = 0;
  feedbackCount: number = 0;
  showFeedbacks: boolean = false;
  errorMessage: string = '';

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private snackBar: MatSnackBar
  ) {}

  /** Inițializează componenta și încarcă datele utilizatorului */
  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('userId');
      if (id) {
        this.userId = +id;
        this.loadUserData();
      }
    });
  }

  /** Încarcă datele publice ale utilizatorului și media ratingului */
  loadUserData(): void {
    this.userService.getUserById(this.userId).subscribe({
      next: data => {
        this.user = data;

        if (this.user.profilePicture) {
          this.user.profilePicture = 'http://localhost:8080' + this.user.profilePicture + '?v=' + new Date().getTime();
        }

        this.loadAverageRating();
      },
      error: err => {
        this.errorMessage = 'Utilizatorul nu a fost găsit.';
        this.snackBar.open(this.errorMessage, 'Închide', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom',
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  /** Încarcă media ratingului și numărul de feedback-uri */
  loadAverageRating(): void {
    this.userService.getAverageRatingForUser(this.userId).subscribe({
      next: (avg) => {
        this.averageRating = avg ?? 0;

        this.userService.getFeedbackCountForUser(this.userId).subscribe({
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

  /** Încarcă lista feedback-urilor primite */
  loadFeedbacks(): void {
    this.userService.getFeedbackForUser(this.userId).subscribe({
      next: (feedbacks) => {
        this.feedbackList = feedbacks;
        this.showFeedbacks = true;
      },
      error: (err) => {
        console.error('Eroare la încărcarea feedback-urilor:', err);
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
