import {
  Component,
  OnInit,
  OnDestroy,
  AfterViewChecked,
  ViewChild,
  ElementRef
} from '@angular/core';
import { ChatService } from '../../services/chat.service';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EventService } from '../../services/event.service';
import { Router } from '@angular/router';
import { Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';

export interface ChatMessage {
  senderId: number;
  senderName: string;
  senderProfilePicture?: string;
  message: string;
  timestamp: string | Date;
}

@Component({
  selector: 'app-chat',
  standalone: false,
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  eventId = 0;
  currentUser: any;
  messages: ChatMessage[] = [];
  newMessage: string = '';
  messageSubscription!: Subscription;

  @ViewChild('scrollMe') private scrollContainer!: ElementRef;

  myEvents: any[] = [];
  selectedEventId: number | null = null;
  selectedEventTitle: string = '';
  selectedEvent: any = null;
  isEventActive: boolean = true;
  selectedEventOrganizerId: number | null = null;
  showMembersDropdown: boolean = false;
  allMembers: any[] = [];
  contextMenuVisible: boolean = false;
  contextMenuX: number = 0;
  contextMenuY: number = 0;
  contextUser: any = null;

  constructor(
    private chatService: ChatService,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    private eventService: EventService,
    private router: Router,
    private dialog: MatDialog,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    // Încarcă datele utilizatorului curent din sessionStorage
    const id = sessionStorage.getItem('userId');
    this.currentUser = {
      id: Number(id),
      role: sessionStorage.getItem('role'),
      firstName: sessionStorage.getItem('firstName'),
      lastName: sessionStorage.getItem('lastName')
    };

    // Obține ID-ul evenimentului din URL și încarcă evenimentele înregistrate
    const urlEventId = Number(this.route.snapshot.paramMap.get('eventId'));
    this.loadRegisteredEvents(urlEventId);

    // Adaugă listener pentru închiderea meniului contextual
    if (isPlatformBrowser(this.platformId)) {
      document.addEventListener('click', this.closeContextMenu.bind(this));
    }
  }

  // Încarcă evenimentele la care utilizatorul este înregistrat sau pe care le-a creat
  loadRegisteredEvents(urlEventId: number): void {
    const userId = this.currentUser.id;
    const role = this.currentUser.role;

    // Determină URL-ul în funcție de rol - asociațiile văd evenimentele create, utilizatorii pe cele înregistrate
    let url = '';
    if (role === 'ASSOCIATION') {
      url = `http://localhost:8080/api/events/created-by/${userId}`;
    } else {
      url = `http://localhost:8080/api/events/registered/${userId}`;
    }

    fetch(url)
      .then(res => res.json())
      .then(events => {
        this.myEvents = events;

        // Selectează evenimentul din URL sau primul disponibil
        const found = events.find((e: any) => e.id === urlEventId);
        if (found) {
          this.selectEvent(found);
        } else if (events.length > 0) {
          this.selectEvent(events[0]);
        }
      });
  }

  // Selectează un eveniment și inițializează chat-ul pentru acesta
  selectEvent(event: any): void {
    this.eventId = event.id;
    this.selectedEventId = event.id;
    this.selectedEventTitle = event.name || event.title || 'Chat';
    this.selectedEvent = event;
    this.isEventActive = event.isActive;

    this.messages = [];

    // Reconectează la chat-ul noului eveniment
    this.chatService.disconnect();
    this.chatService.connect(this.eventId);
    this.messageSubscription?.unsubscribe();
    this.selectedEventOrganizerId = event.organizerId;
    this.loadAllMembers(event.id);

    // Încarcă istoricul mesajelor și se abonează la mesaje noi
    this.chatService.getChatHistory(this.eventId).subscribe(history => {
      this.messages = history;
      setTimeout(() => this.scrollToBottom(), 0);
    });

    this.messageSubscription = this.chatService.getMessages().subscribe(msg => {
      this.messages.push(msg);
      setTimeout(() => this.scrollToBottom(), 0);
    });
  }

  // Trimite un mesaj nou în chat
  sendMessage(): void {
    if (!this.isEventActive || this.newMessage.trim() === '') return;

    const msg = {
      eventId: this.eventId,
      senderId: this.currentUser.id,
      senderName: this.currentUser.firstName + ' ' + this.currentUser.lastName,
      message: this.newMessage,
      timestamp: new Date()
    };

    this.chatService.sendMessage(this.eventId, msg);
    this.newMessage = '';
  }

  // Încarcă toți membrii evenimentului selectat
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
    this.contextMenuVisible = false;
    if (!this.contextUser) return;
  
    if (this.contextUser.id === this.currentUser.id) {
      this.router.navigate(['/profil']);
    } else {
      this.router.navigate(['/public-profile', this.contextUser.id]);
    }
  }

  // Elimină un utilizator din eveniment (doar pentru organizatori)
  kickUser(): void {
    if (!this.contextUser || !this.selectedEventId) return;
  
    // Afișează dialog de confirmare
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '350px',
      data: {
        title: 'Confirmare eliminare',
        message: `Sigur vrei să îl elimini pe ${this.contextUser.firstName} din eveniment?`,
        confirmButtonText: 'Elimină'
      }
    });
  
    dialogRef.afterClosed().subscribe((result: boolean) => {
      if (!result) return;
  
      // Elimină utilizatorul din eveniment
      this.eventService.kickUserFromEvent(this.selectedEventId!, this.contextUser.id).subscribe({
        next: () => {
          this.snackBar.open('Utilizatorul a fost eliminat cu succes.', 'Închide', {
            duration: 3000,
            panelClass: ['snackbar-success']
          });
          // Actualizează listele locale
          this.allMembers = this.allMembers.filter(m => m.id !== this.contextUser.id);
          this.messages = this.messages.filter(msg => msg.senderId !== this.contextUser.id);
          this.contextMenuVisible = false;
        },
        error: err => {
          console.error(err);
          this.snackBar.open('Eroare la eliminarea utilizatorului.', 'Închide', {
            duration: 3000,
            panelClass: ['snackbar-error']
          });
        }
      });
    });
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

  ngOnDestroy(): void {
    // Cleanup la distrugerea componentei
    this.chatService.disconnect();
    this.messageSubscription?.unsubscribe();
    if (isPlatformBrowser(this.platformId)) {
      document.removeEventListener('click', this.closeContextMenu.bind(this));
    }
  }
}