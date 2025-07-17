import {
  Component, OnInit, OnDestroy, AfterViewChecked, ViewChild, ElementRef
} from '@angular/core';
import { ChatService } from '../../services/chat.service';
import { Subscription } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EventFeedbackService } from '../../services/event-feedback.service';
import { EventService } from '../../services/event.service';
import { Router } from '@angular/router';
import { CertificateService } from '../../services/certificate.service';
export interface ChatMessage {
  senderId: number;
  senderName: string;
  senderProfilePicture?: string;
  message: string;
  timestamp: string | Date;
}

@Component({
  selector: 'app-archived-chat',
  standalone: false,
  templateUrl: './archived-events.component.html',
  styleUrls: ['./archived-events.component.css']
})
export class ArchivedEventsComponent implements OnInit, OnDestroy, AfterViewChecked {
  messages: ChatMessage[] = [];
  messageSubscription!: Subscription;

  @ViewChild('scrollMe') private scrollContainer!: ElementRef;
  feedbackContext: 'TO_ORGANIZER' | 'TO_PARTICIPANT' = 'TO_ORGANIZER';
  archivedEvents: any[] = [];
  selectedEventId: number | null = null;
  selectedEventTitle: string = '';
  selectedEvent: any = null;
  showFeedbackModal = false;
  selectedEventIdForFeedback!: number;
  organizerId!: number;
  feedbackReceiverId!: number;
  hasGivenFeedback: boolean = false;
  currentUserId: number = Number(sessionStorage.getItem('userId'));
  userRole: string | null = sessionStorage.getItem('role');
  showParticipants: boolean = false;
  participants: any[] = [];
  showMembersDropdown: boolean = false;
  allMembers: any[] = [];
  showParticipantsModal: boolean = false; 
  contextMenuVisible: boolean = false;
  contextMenuX: number = 0;
  contextMenuY: number = 0;
  contextUser: any = null;
  showCertificatesSection = false;
  organizationName: string = '';
  stampFile: File | null = null;
  participantsWithHours: any[] = [];
  selectedEventOrganizerId: number | null = null;

  constructor(
    private chatService: ChatService,
    private snackBar: MatSnackBar,
    private feedbackService: EventFeedbackService,
    private eventService: EventService,
    private router: Router,
    private certificateService: CertificateService
  ) {}

  ngOnInit(): void {
    // Încarcă evenimentele arhivate și adaugă listener pentru meniul contextual
    this.loadArchivedEvents();
    document.addEventListener('click', this.closeContextMenu.bind(this));
  }

  // Încarcă evenimentele arhivate în funcție de rolul utilizatorului
  loadArchivedEvents(): void {
    const userId = this.currentUserId;
    const role = this.userRole;
    let url = '';

    // Determină URL-ul în funcție de rol - asociațiile văd evenimentele create, utilizatorii pe cele înregistrate
    if (role === 'ASSOCIATION') {
      url = `http://localhost:8080/api/events/archived/created-by/${userId}`;
    } else {
      url = `http://localhost:8080/api/events/archived/registered/${userId}`;
    }

    fetch(url)
      .then(res => res.json())
      .then(events => {
        this.archivedEvents = events;
        if (events.length > 0) {
          this.selectEvent(events[0]);
        }
      });
  }

  // Selectează un eveniment arhivat și încarcă istoricul chat-ului
  selectEvent(event: any): void {
    this.selectedEventId = event.id;
    this.selectedEventTitle = event.name || 'Archived Chat';
    this.selectedEvent = event;
    this.selectedEventOrganizerId = event.organizerId;

    this.messages = [];
    this.chatService.disconnect();

    // Încarcă istoricul mesajelor pentru evenimentul selectat
    this.chatService.getChatHistory(event.id).subscribe(history => {
      this.messages = history;
      setTimeout(() => this.scrollToBottom(), 0);
    });

    if (this.messageSubscription) this.messageSubscription.unsubscribe();

    this.loadAllMembers(event.id);

    // Verifică statusul feedback-ului pentru utilizatori sau încarcă participanții pentru asociații
    if (this.userRole === 'USER') {
      this.feedbackService.getFeedbackFromUser(this.currentUserId).subscribe({
        next: (feedbackList: any[]) => {
          this.hasGivenFeedback = feedbackList.some(f => f.eventId === event.id);
        },
        error: () => {
          this.hasGivenFeedback = false;
        }
      });
    } else if (this.userRole === 'ASSOCIATION') {
      this.loadParticipants(event.id);
    }
  }

  // Deschide modalul pentru trimiterea feedback-ului către organizator
  openFeedback(event: any): void {
    this.selectedEventIdForFeedback = event.id;
    this.feedbackReceiverId = event.organizer?.id;
    this.feedbackContext = 'TO_ORGANIZER';
    this.showFeedbackModal = true;
  }

  // Deschide modalul pentru trimiterea feedback-ului către un participant
  giveFeedbackToParticipant(participant: any): void {
    if (participant.feedbackGiven) return;
    this.selectedEventIdForFeedback = this.selectedEventId!;
    this.feedbackReceiverId = participant.id;
    this.feedbackContext = 'TO_PARTICIPANT';
    this.showFeedbackModal = true;
  }

  // Trimite feedback-ul către backend și actualizează UI-ul
  handleFeedbackSubmit(dto: any): void {
    const submitFn = this.feedbackContext === 'TO_PARTICIPANT'
      ? this.feedbackService.submitFeedbackToParticipant(this.feedbackReceiverId, dto)
      : this.feedbackService.submitFeedback(dto);

    submitFn.subscribe({
      next: () => {
        this.snackBar.open('Feedback trimis cu succes!', 'Închide', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom'
        });
        this.showFeedbackModal = false;

        // Actualizează statusul feedback-ului
        if (this.feedbackContext === 'TO_PARTICIPANT') {
          this.loadParticipants(this.selectedEventId!);
        } else {
          this.hasGivenFeedback = true;
        }
      },
      error: err => {
        const errorMsg = err.error?.error || err.error || 'A apărut o eroare necunoscută.';
        this.snackBar.open(`Eroare: ${errorMsg}`, 'Închide', {
          duration: 4000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom',
          panelClass: ['snackbar-error']
        });
      }
    });
  }

  // Încarcă participanții evenimentului și verifică statusul feedback-ului primit
  loadParticipants(eventId: number): void {
    this.eventService.getParticipants(eventId).subscribe({
      next: (data: any[]) => {
        this.participants = data;
        // Verifică dacă s-a dat deja feedback pentru fiecare participant
        this.feedbackService.getFeedbackForOrganizer(this.currentUserId).subscribe({
          next: (feedbackList: any[]) => {
            this.participants.forEach(participant => {
              const feedbackExists = feedbackList.some(f =>
                f.eventId === eventId &&
                f.toUserId === participant.id
              );
        
              participant.feedbackGiven = feedbackExists;
            });
          }
        });
        
      },
      error: () => {
        this.snackBar.open('Eroare la încărcarea participanților', 'Închide', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
      }
    });
  }

  // Încarcă toți membrii evenimentului pentru afișare în dropdown
  loadAllMembers(eventId: number): void {
    this.eventService.getAllMembers(eventId).subscribe({
      next: (data: any[]) => {
        this.allMembers = data;
      },
      error: err => {
        console.error('Eroare la încărcarea membrilor:', err);
        this.snackBar.open('Eroare la încărcarea membrilor', 'Închide', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
      }
    });
  }

  // Comută afișarea dropdown-ului cu membri
  toggleMembersDropdown(): void {
    this.showMembersDropdown = !this.showMembersDropdown;
  }

  // Gestionează click dreapta pe un utilizator pentru afișarea meniului contextual
  onRightClick(event: MouseEvent, user: any): void {
    event.preventDefault();
    this.contextUser = user;
    this.contextMenuX = event.clientX;
    this.contextMenuY = event.clientY;
    this.contextMenuVisible = true;
  }

  // Navighează la profilul utilizatorului selectat
  goToUserProfile(): void {
    if (!this.contextUser) return;

    this.contextMenuVisible = false;

    if (this.contextUser.id === this.currentUserId) {
      this.router.navigate(['/profil']);
    } else {
      this.router.navigate(['/public-profile', this.contextUser.id]);
    }
  }

  // Închide meniul contextual
  closeContextMenu(): void {
    this.contextMenuVisible = false;
  }

  ngAfterViewChecked(): void {
    // Auto-scroll la sfârșitul chat-ului după fiecare verificare
    this.scrollToBottom();
  }

  // Face scroll automat la sfârșitul containerului de mesaje
  private scrollToBottom(): void {
    try {
      const el = this.scrollContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch (err) {
      console.warn('Scroll failed:', err);
    }
  }

  // Comută afișarea secțiunii cu participanți
  toggleParticipants(): void {
    this.showParticipants = !this.showParticipants;
  }

  // Deschide modalul pentru feedback-ul participanților
  openParticipantsFeedbackPopup(): void {
    this.showParticipantsModal = true;
  }
  
  // Închide modalul pentru feedback-ul participanților
  closeParticipantsFeedbackPopup(): void {
    this.showParticipantsModal = false;
  }

  // Deschide secțiunea pentru generarea adeverințelor
  openCertificatesSection(): void {
    this.organizationName = '';
    this.stampFile = null;
    this.participantsWithHours = this.participants.map(p => ({
      userId: p.id,
      firstName: p.firstName,
      lastName: p.lastName,
      profilePicture: p.profilePicture,
      hours: 0
    }));
    this.showCertificatesSection = true;
  }

  // Trimite cererea pentru generarea adeverințelor
  submitCertificates(): void {
    if (!this.organizationName) {
      this.snackBar.open('Completează numele organizației!', 'Închide', {
        duration: 3000,
        horizontalPosition: 'center',
        verticalPosition: 'top'
      });
      return;
    }
  
    // Încarcă ștampila dacă este selectată, apoi generează adeverințele
    if (this.stampFile) {
      this.certificateService.uploadStamp(this.selectedEventId!, this.stampFile).subscribe({
        next: () => this.sendCertificates(),
        error: err => {
          this.snackBar.open('Eroare la încărcarea ștampilei: ' + (err.error || err.message), 'Închide', {
            duration: 3000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['snackbar-error']
          });
        }
      });
    } else {
      this.sendCertificates();
    }
  }
  
  // Trimite cererea pentru generarea adeverințelor către backend
  sendCertificates(): void {
    const request = {
      organizationName: this.organizationName,
      participants: this.participantsWithHours.map(p => ({
        userId: p.userId,
        hours: p.hours
      }))
    };
  
    this.certificateService.generateCertificates(this.selectedEventId!, request).subscribe({
      next: () => {
        if (this.selectedEvent) {
          this.selectedEvent.certificatesGenerated = true;
        }
  
        this.showCertificatesSection = false;
        this.loadParticipants(this.selectedEventId!);
  
        this.snackBar.open('Adeverințele au fost trimise cu succes!', 'Închide', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      },
      error: err => {
        this.snackBar.open('Eroare la generare adeverințe: ' + (err.error || err.message), 'Închide', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top',
          panelClass: ['snackbar-error']
        });
      }
    });
  }
  
  // Gestionează selectarea fișierului cu ștampila
  onStampSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.stampFile = file;
    }
  }
  
  // Elimină un participant din lista pentru adeverințe
  removeParticipantFromCertificates(index: number): void {
    this.participantsWithHours.splice(index, 1);
  }
  
  ngOnDestroy(): void {
    // Cleanup la distrugerea componentei
    this.chatService.disconnect();
    this.messageSubscription?.unsubscribe();
    document.removeEventListener('click', this.closeContextMenu.bind(this));
  }
}