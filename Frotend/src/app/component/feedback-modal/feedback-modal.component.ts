import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-feedback-modal',
  standalone: false,
  templateUrl: './feedback-modal.component.html',
  styleUrls: ['./feedback-modal.component.css']
})
export class FeedbackModalComponent {
  // Arată sau ascunde fereastra modală (true = deschisă, false = închisă)
  @Input() visible: boolean = false;

  // ID-ul evenimentului pentru care se oferă feedback
  @Input() eventId!: number;

  // ID-ul utilizatorului care trimite feedback-ul
  @Input() fromUserId!: number;

  // ID-ul utilizatorului care primește feedback-ul
  @Input() toUserId!: number;

  // Specifică direcția feedbackului: către organizator sau către participant
  @Input() context: 'TO_ORGANIZER' | 'TO_PARTICIPANT' = 'TO_ORGANIZER';

  // Emitere către componenta părinte când se închide modalul
  @Output() close = new EventEmitter<void>();

  // Emitere către componenta părinte când se trimite feedbackul
  @Output() submitted = new EventEmitter<any>();

  // Rating-ul dat de utilizator (implicit 5 stele)
  rating: number = 5;

  // Comentariul scris de utilizator
  comment: string = '';

  // Se apelează când utilizatorul apasă „Trimite” – construiește obiectul și îl trimite
  submit() {
    if (this.rating < 1 || this.rating > 5) return;

    const dto = {
      eventId: this.eventId,
      fromUserId: this.fromUserId,
      toUserId: this.toUserId,
      rating: this.rating,
      comment: this.comment
    };

    this.submitted.emit(dto);  // Trimitem feedbackul completat
    this.close.emit();         // Închidem modalul
  }

  // Se apelează când utilizatorul apasă „Anulează” – doar închide modalul
  cancel() {
    this.close.emit();
  }
}
