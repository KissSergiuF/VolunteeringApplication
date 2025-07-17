import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  standalone: false,
  styleUrls: ['./confirm-dialog.component.scss']
})
export class ConfirmDialogComponent {
  // Constructorul primește referința la dialog și datele injectate (titlu, mesaj, textul butonului)
  constructor(
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      title: string;
      message: string;
      confirmButtonText?: string;
    }
  ) {}

  // Apelează când utilizatorul confirmă acțiunea
  onConfirm(): void {
    this.dialogRef.close(true);
  }

  // Apelează când utilizatorul anulează acțiunea
  onCancel(): void {
    this.dialogRef.close(false);
  }
}
