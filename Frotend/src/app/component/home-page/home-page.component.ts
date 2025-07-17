import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { EventService } from '../../services/event.service';
import { EventRegistrationService } from '../../services/event-registration.service';
import { EventFeedbackService } from '../../services/event-feedback.service';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-home-page',
  standalone: false,
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css'
})
export class HomePageComponent {

  constructor(
    private router: Router,
    private eventService: EventService,
    private eventRegistrationService: EventRegistrationService,
    private eventFeedbackService: EventFeedbackService,
    private userService: UserService
  ) {}

  isLoggedIn = false;
  userRole: string | null = null;
  userId: number | null = null;
  registeredEvents: any[] = [];
  userFeedbacks: any[] = [];
  loading = true;
  createdEvents: any[] = [];
  organizerFeedbacks: any[] = [];

  slides = [
    { title: 'Conectează-te', text: 'Platforma noastră îți permite să te conectezi rapid cu organizații care au nevoie de sprijin...' },
    { title: 'Descoperă oportunități', text: 'Exploră harta interactivă pentru a găsi evenimente de voluntariat...' },
    { title: 'Fă o diferență', text: 'Alătură-te acțiunilor de voluntariat și ajută la construirea unei lumi mai bune...' },
    { title: 'Înregistrează-te ca voluntar', text: 'Creează-ți un profil personalizat, explorează activitățile disponibile...' },
    { title: 'Fii o organizație activă', text: 'Organizațiile pot adăuga evenimente pe hartă, comunicând direct cu voluntarii...' },
    { title: 'Comunică ușor', text: 'Fiecare proiect are un chat dedicat în care voluntarii și organizatorii pot colabora...' }
  ];

  currentSlideIndex = 0;

  // Comută la slide-ul anterior din carousel
  prevSlide() {
    this.currentSlideIndex =
      (this.currentSlideIndex - 1 + this.slides.length) % this.slides.length;
  }

  // Comută la slide-ul următor din carousel
  nextSlide() {
    this.currentSlideIndex = (this.currentSlideIndex + 1) % this.slides.length;
  }

  // Comută direct la un anumit slide
  goToSlide(index: number) {
    this.currentSlideIndex = index;
  }

  // Navighează către pagina de login, cu rolul presetat
  goToLogin(role: 'USER' | 'ASSOCIATION') {
    this.router.navigate(['/login'], {
      state: {
        form: 'register',
        role: role
      }
    });
  }

  // Navighează către pagina cu harta
  goToMap(): void {
    this.router.navigate(['/map']);
  }

  faqs = [
    {
      question: 'Cum mă pot înscrie ca voluntar?',
      answer: 'Apasă pe butonul "Devino voluntar", creează un cont și completează profilul tău.',
      open: false
    },
    {
      question: 'Organizația mea poate înregistra evenimente?',
      answer: 'Desigur! Creează un cont pentru organizație și adaugă evenimentele pe hartă.',
      open: false
    },
    {
      question: 'Cât costă să folosesc această platformă?',
      answer: 'Platforma este complet gratuită pentru voluntari și organizații.',
      open: false
    }
  ];

  // Deschide/închide un răspuns din secțiunea de întrebări frecvente
  toggleFAQ(index: number) {
    this.faqs[index].open = !this.faqs[index].open;
  }

  // Inițializează componenta și încarcă contextul utilizatorului
  ngOnInit(): void {
    this.loadUserContext();

    if (this.isLoggedIn && this.userRole === 'USER') {
      this.loadVolunteerData();
    } else if (this.isLoggedIn && this.userRole === 'ASSOCIATION') {
      this.loadOrganizationData();
    }

    this.loading = false;
  }

  // Încarcă din sessionStorage ID-ul și rolul utilizatorului
  loadUserContext(): void {
    const storedUserId = sessionStorage.getItem('userId');
    const storedRole = sessionStorage.getItem('role');

    if (storedUserId && storedRole) {
      this.isLoggedIn = true;
      this.userId = +storedUserId;
      this.userRole = storedRole;
    }
  }

  // Încarcă evenimentele și feedback-urile utilizatorului (voluntar)
  loadVolunteerData(): void {
    if (!this.userId) return;

    this.eventRegistrationService.getRegisteredEventIds(this.userId).subscribe((eventIds: number[]) => {
      this.registeredEvents = [];

      eventIds.forEach(eventId => {
        this.eventService.getEventById(eventId).subscribe(event => {
          this.registeredEvents.push(event);
        });
      });
    });

    this.userService.getFeedbackForUser(this.userId).subscribe(feedbacks => {
      this.userFeedbacks = feedbacks;
    });
  }

  // Încarcă evenimentele create și feedback-urile organizației
  loadOrganizationData(): void {
    if (!this.userId) return;

    this.eventService.getAvailableEvents().subscribe(events => {
      this.createdEvents = events.filter(e => e.organizerId === this.userId);
    });

    this.userService.getFeedbackForUser(this.userId).subscribe(feedbacks => {
      this.organizerFeedbacks = feedbacks;
    });
  }
}
