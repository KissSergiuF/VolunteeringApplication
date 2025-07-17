import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ContactService } from '../../services/contact.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-contact',
  standalone: false,
  templateUrl: './contact-us.component.html',
  styleUrls: ['./contact-us.component.css']
})
export class ContactUsComponent {
  // Formularul de contact cu validări
  contactForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private contactService: ContactService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.contactForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.pattern(/^((\+[\d]{2,4}\d{9})|(0\d{9}))$/)]],
      message: ['', Validators.required]
    });
  }

  // Trimite formularul după confirmarea în dialog
  onSubmit() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Confirmare',
        message: 'Ești sigur că vrei să trimiți acest formular?',
        confirmButtonText: 'Trimite'
      }
    });
  
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (this.contactForm.valid) {
          this.contactService.sendContactMessage(this.contactForm.value)
            .subscribe({
              next: (response) => {
                console.log('Success response:', response);
                this.snackBar.open('Mulțumesc pentru mesajul tău! Emailul a fost trimis.', 'Ok', {
                  duration: 5000,
                }); 
                this.contactForm.reset();
              },
              error: (err) => {
                console.error('Error:', err);
              }
            });
        }
      }
    });
  }
}
