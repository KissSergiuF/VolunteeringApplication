import {
  Component, OnInit, AfterViewInit, ElementRef, ViewChild, NgZone,
  PLATFORM_ID, Inject, OnDestroy
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { EventService } from '../../services/event.service';
import { EventRegistrationService } from '../../services/event-registration.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';
@Component({
  selector: 'app-map',
  standalone: false,
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('searchBox') searchBox!: ElementRef;
  @ViewChild('gmap') gmapElement: any;
  
  private autocompleteInstance: any = null;
  private googleMapsApiCheckInterval: any = null;
  private geocoder: any = null;

  constructor(
    private eventService: EventService,
    private ngZone: NgZone,
    private eventRegistrationService: EventRegistrationService,
    @Inject(PLATFORM_ID) private platformId: Object,
    private snackBar: MatSnackBar,
    private router: Router,
    private dialog: MatDialog
  ) {}

  center = { lat: 45.75, lng: 21.23 };
  zoom = 13;
  mapOptions = {};
  googleMap: any;

  events: any[] = [];
  registeredEventIds: number[] = [];

  userRole = '';
  userID: string | null | undefined;
  showEventForm = false;
  tempMarker: { lat: number; lng: number } | null = null;
  dateError: string | null = null;
  durationType: 'oneDay' | 'multipleDays' = 'oneDay';
  minDate: string | undefined;
  selectedCategory: string = '';
  cityCounts: { [city: string]: number } = {};
  availableCities: string[] = [];
  startTime: string = '';
  endTime: string = '';
  selectedEvent: any = null;
  selectedCity: string = '';
  selectedDate: string = '';
  selectedFilterType: string = 'all';
  subscribedEventIds: number[] = [];
  categories: string[] = [
    'Sănătate',
    'Educație',
    'Mediu',
    'Comunitate',
    'Cultură',
    'Sport',
    'Social',
    'Tehnologie',
    'Caritate',
    'Artă',
    'Animale',
    'Dezvoltare personală',
    'Altele' 
  ];
  
  newEvent = {
    name: '',
    description: '',
    category: '',
    location: '',
    startDate: '',
    endDate: '',
    lat: 0,
    lng: 0,
    maxVolunteers: null
  };

  /** Inițializează datele componentei și setările hărții */
  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      const role = sessionStorage.getItem('role');
      const id = sessionStorage.getItem('userId');
      this.userRole = role === 'ASSOCIATION' ? 'association' : 'volunteer';
      this.userID = id;
      if (typeof google === 'undefined' || typeof google.maps === 'undefined') {
        this.setupGoogleMapsApiCheck();
      } else {
        this.initGeocoder();
      }
    }
    this.loadEvents();
    this.loadRegisteredEvents();
    this.loadSubscribedEvents();
    const today = new Date();
    this.minDate = today.toISOString().split('T')[0];
  }

  /** Inițializează autocomplete-ul și geocoder-ul după afișarea vederii */
  ngAfterViewInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      const attempts = [100, 500, 1000, 2000]; 
      attempts.forEach(delay => {
        setTimeout(() => {
          if (typeof google !== 'undefined' && typeof google.maps !== 'undefined') {
            this.initializeAutocomplete();
            this.initGeocoder();
          }
        }, delay);
      });
    }
  }

  /** Dezactivează verificarea Google Maps API la distrugerea componentei */
  ngOnDestroy(): void {
    if (this.googleMapsApiCheckInterval) {
      clearInterval(this.googleMapsApiCheckInterval);
    }
  }
  
  /** Verifică periodic dacă Google Maps API este disponibil */
  setupGoogleMapsApiCheck(): void {
    this.googleMapsApiCheckInterval = setInterval(() => {
      if (typeof google !== 'undefined' && typeof google.maps !== 'undefined' && 
          typeof google.maps.places !== 'undefined') {
        clearInterval(this.googleMapsApiCheckInterval);
        this.initializeAutocomplete();
        this.initGeocoder();
      }
    }, 1000);
  }
  

  /** Inițializează serviciul de geocodificare Google Maps */
  initGeocoder(): void {
    if (typeof google !== 'undefined' && typeof google.maps !== 'undefined') {
      this.geocoder = new google.maps.Geocoder();
    }
  }
  
  /** Verifică încărcarea corectă a Google Maps și inițializează autocomplete-ul */
  checkGoogleMapsApiAndInitialize(): void {
    if (typeof google === 'undefined' || typeof google.maps === 'undefined') {
      console.error('API-ul Google Maps nu s-a încărcat! Verificați includerea scriptului în index.html');
      return;
    }
    
    if (typeof google.maps.places === 'undefined') {
      console.error('Biblioteca Google Maps Places nu s-a încărcat! Asigurați-vă că includeți &libraries=places în URL-ul API-ului');
      return;
    }
    
    this.initializeAutocomplete();
    this.initGeocoder();
  }

  /** Inițializează funcția de autocomplete pentru câmpul de locație */
  initializeAutocomplete(): void {
    if (!isPlatformBrowser(this.platformId) || typeof google === 'undefined') {
      console.error('Nu se poate inițializa autocomplete: platforma nu este browser sau API-ul Google nu s-a încărcat');
      return;
    }

    if (this.autocompleteInstance && this.showEventForm) {
      return;
    }
    setTimeout(() => {
      const input = this.searchBox?.nativeElement;
      if (!input) {
        console.error('Elementul search box nu a fost găsit în DOM');
        return;
      }
      
      console.log('Se inițializează Places Autocomplete pe element:', input);
      
      try {
        this.autocompleteInstance = new google.maps.places.Autocomplete(input);
        
        this.autocompleteInstance.addListener('place_changed', () => {
          this.ngZone.run(() => {
            const place = this.autocompleteInstance.getPlace();
            console.log('Locația selectată:', place);
            
            if (!place.geometry) {
              console.error("Nu sunt date geometrice disponibile pentru această locație");
              return;
            }
            
            const lat = place.geometry.location.lat();
            const lng = place.geometry.location.lng();

            this.newEvent.lat = lat;
            this.newEvent.lng = lng;
            this.newEvent.location = place.formatted_address || place.name || '';

            this.center = { lat, lng };
            this.tempMarker = { lat, lng };
            
            if (this.gmapElement?._googleMap) {
              this.gmapElement._googleMap.panTo({ lat, lng });
              this.gmapElement._googleMap.setZoom(15); 
            }
          });
        });
        
        setTimeout(() => {
          const pacContainers = document.querySelectorAll('.pac-container');
          pacContainers.forEach((container) => {
            (container as HTMLElement).style.zIndex = '10000';
          });
        }, 300);
        
      } catch (error) {
        console.error('Eroare la inițializarea Google Places Autocomplete:', error);
      }
    }, 300);
  }

  /** Deschide formularul de adăugare eveniment */
  openEventForm(): void {
    this.resetForm();
    this.showEventForm = true;
    const delays = [300, 600, 1000];
    delays.forEach(delay => {
      setTimeout(() => {
        this.initializeAutocomplete();
        const pacContainers = document.querySelectorAll('.pac-container');
        pacContainers.forEach((container: any) => {
          if (container && container.style) {
            container.style.zIndex = '10000';
          }
        });
      }, delay);
    });
  }

  /** Închide formularul de adăugare eveniment */
  closeEventForm(): void {
    this.showEventForm = false;
    this.tempMarker = null;
    this.autocompleteInstance = null;
  }

  /** Resetează câmpurile formularului pentru un eveniment nou */
  resetForm(): void {
    this.newEvent = {
      name: '',
      description: '',
      category: '',
      location: '',
      startDate: '',
      endDate: '',
      lat: 0,
      lng: 0,
      maxVolunteers: null
    };
    this.tempMarker = null;
    this.dateError = null;
    this.durationType = 'oneDay';
  }

  /** Validează datele și orele introduse pentru eveniment */
  validateDates(): void {
    this.dateError = null;
  
    const startDate = this.newEvent.startDate;
    const endDate = this.newEvent.endDate;
    const startTime = this.startTime;
    const endTime = this.endTime;
  
    if (this.durationType === 'multipleDays') {
      if (startDate && endDate) {
        const start = new Date(startDate);
        const end = new Date(endDate);

        if (start.getTime() >= end.getTime()) {
          this.dateError = "Data de sfârșit trebuie să fie după data de început (minim 1 zi diferență)";
          return;
        }
      }
    }
  
    if (startDate && startTime && endTime) {
      const startDateTime = new Date(`${startDate}T${startTime}`);
      const endDateTime = new Date(`${endDate || startDate}T${endTime}`);
  
      if (startDateTime >= endDateTime) {
        this.dateError = "Ora de sfârșit trebuie să fie după ora de început";
      }
    }
  }
  

  /** Trimite cererea de creare a unui eveniment */
  submitEvent(): void {
    if (this.durationType === 'oneDay') {
      this.newEvent.endDate = this.newEvent.startDate;
    } else {
      this.validateDates(); 
    }
  
    const isValid =
      this.tempMarker &&
      this.newEvent.name &&
      this.newEvent.description &&
      this.newEvent.category &&
      this.newEvent.location &&
      this.newEvent.startDate &&
      this.startTime &&
      this.endTime &&
      !this.dateError &&
      (this.durationType === 'oneDay' || this.newEvent.endDate); 
  
    if (isValid) {
      const formatLocalDateTime = (dateStr: string, timeStr: string) => {
        const pad = (n: number) => n.toString().padStart(2, '0');
        const [year, month, day] = dateStr.split('-').map(Number);
        const [hour, minute] = timeStr.split(':').map(Number);
        return `${year}-${pad(month)}-${pad(day)}T${pad(hour)}:${pad(minute)}`;
      };
  
      const eventData = {
        name: this.newEvent.name,
        description: this.newEvent.description,
        category: this.newEvent.category,
        location: this.newEvent.location,
        lat: this.newEvent.lat,
        lng: this.newEvent.lng,
        startDate: formatLocalDateTime(this.newEvent.startDate, this.startTime),
        endDate: formatLocalDateTime(this.newEvent.endDate || this.newEvent.startDate, this.endTime),
        maxVolunteers: this.newEvent.maxVolunteers || null 
      };
  
      this.eventService.addEvent(eventData).subscribe({
        next: (response) => {
          console.log('Evenimentul a fost creat cu succes:', response);
          this.snackBar.open("Evenimentul a fost adăugat cu succes!", "Închide", {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
          this.loadEvents();
          this.closeEventForm();
        },
        error: (error) => {
          console.error('Eroare la crearea evenimentului:', error);
        }
      });
    }
  }
  
  /** Returnează numărul de voluntari înregistrați pentru un eveniment */
  getVolunteerCount(eventId: number): number {
    const event = this.events.find(e => e.id === eventId);
    return event && event.currentVolunteers ? event.currentVolunteers : 0;
  }
  
  
  
  /** Comută selecția pe un eveniment afișat pe hartă */
  toggleEventSelection(event: any): void {
    if (this.selectedEvent === event) {
      this.selectedEvent = null;
      if (this.gmapElement?._googleMap) {
        this.gmapElement._googleMap.setZoom(13);
      }
      this.center = { lat: 45.75, lng: 21.23 };
    } else {
      this.selectedEvent = event;
      this.center = { lat: event.lat, lng: event.lng };
      
      if (this.gmapElement?._googleMap) {
        this.gmapElement._googleMap.setZoom(15);
      }
    }
  }

  /** Transformă coordonatele în nume de oraș folosind Geocoder */
  getCityFromCoordinates(lat: number, lng: number): Promise<string> {
    return new Promise((resolve, reject) => {
      if (!this.geocoder) {
        this.initGeocoder();
        if (!this.geocoder) {
          reject("Geocoder-ul nu a fost inițializat");
          return;
        }
      }
  
      this.geocoder.geocode({ location: { lat, lng } }, (results: any, status: any) => {
        if (status === 'OK' && results[0]) {
          const components = results[0].address_components;
  
          const cityComponent = components.find((c: any) => c.types.includes('locality')) ||
                                components.find((c: any) => c.types.includes('administrative_area_level_2')) ||
                                components.find((c: any) => c.types.includes('administrative_area_level_1'));
  
          if (cityComponent) {
            resolve(cityComponent.long_name);
          } else {
            resolve('Oraș necunoscut');
          }
        } else {
          console.error("Geocoder-ul a eșuat: " + status);
          resolve('Oraș necunoscut');
        }
      });
    });
  }
  
  
  /** Încarcă toate evenimentele disponibile și actualizează filtrele pe orașe */
  loadEvents(): void {
    this.eventService.getAvailableEvents().subscribe({
      next: (events) => {
        this.events = events;
        const geocodingPromises: Promise<void>[] = [];
        
        events.forEach((event: any) => {
          this.eventService.checkIfUserIsBanned(event.id).subscribe({
            next: (result) => {
              event.banned = result?.banned ?? false;
            },
            error: () => {
              event.banned = false;
            }
          });

          if (event.lat && event.lng) {
            const promise = this.getCityFromCoordinates(event.lat, event.lng)
              .then(city => {
                if (city) {
                  event.cityName = city;
                }
              });
            geocodingPromises.push(promise);
          }
        });
  
        Promise.all(geocodingPromises)
          .then(() => {
            this.updateCityFilters();
          })
          .catch(err => {
            console.error('Eroare în timpul geocodificării:', err);
            this.updateCityFilters(); 
          });
      },
      error: (error) => {
        console.error('Eroare la încărcarea evenimentelor:', error);
      }
    });
  }
  
  
  /** Returnează evenimentele filtrate după oraș, categorie, dată și stare */
  filteredEvents() {
    let result = this.events;
  
    if (this.selectedCategory) {
      result = result.filter(event => event.category === this.selectedCategory);
    }
  
    if (this.selectedCity) {
      result = result.filter(event => event.cityName === this.selectedCity);
    }
  
    if (this.selectedDate) {
      const selected = new Date(this.selectedDate);
  
      result = result.filter(event => {
        const start = new Date(event.startDate);
        const end = new Date(event.endDate);
        const selectedDateOnly = new Date(selected.getFullYear(), selected.getMonth(), selected.getDate());
        const startDateOnly = new Date(start.getFullYear(), start.getMonth(), start.getDate());
        const endDateOnly = new Date(end.getFullYear(), end.getMonth(), end.getDate());
  
        return selectedDateOnly >= startDateOnly && selectedDateOnly <= endDateOnly;
      });
    }
  
    if (this.selectedFilterType === 'my') {
      if (this.userRole === 'volunteer') {
        result = result.filter(event => this.isRegistered(event.id));
      } else if (this.userRole === 'association') {
        result = result.filter(event => event.organizerId === Number(this.userID));
      }
    } else if (this.selectedFilterType === 'available') {
      result = result.filter(event => !event.isFull);
    } else if (this.selectedFilterType === 'full') {
      result = result.filter(event => event.isFull);
    } else if (this.selectedFilterType === 'subscribed') {
      result = result.filter(event => this.isSubscribed(event.id));
    }
  
    return result;
  }
  
  
  /** Înregistrează utilizatorul curent la un eveniment */
  registerToEvent(eventId: number) {
    const userId = this.userID ? Number(this.userID) : null;
    console.log("ID utilizator din sesiune:", this.userID);
    console.log("ID utilizator procesat:", userId);
    console.log("ID eveniment primit:", eventId); 
  
    if (userId === null || isNaN(userId)) {
      alert("ID-ul utilizatorului este invalid. Te rugăm să te autentifici din nou.");
      return;
    }
  
    if (eventId === undefined || eventId === null) {
      alert("ID-ul evenimentului lipsește!");
      return;
    }
  
    this.eventRegistrationService.registerToEvent(eventId, userId)
      .subscribe({
        next: () => {
          this.registeredEventIds.push(eventId);
          this.subscribedEventIds = this.subscribedEventIds.filter(id => id !== eventId);
          this.eventService.getEventById(eventId).subscribe(updatedEvent => {
            const index = this.events.findIndex(ev => ev.id === eventId);
            if (index !== -1) {
              this.events[index] = updatedEvent;
            }
            if (this.selectedEvent?.id === eventId) {
              this.selectedEvent = updatedEvent;
            }
          });
  
          this.snackBar.open("Te-ai înregistrat cu succes la eveniment!", "Închide", {
            duration: 3000,
            panelClass: ['snackbar-success']
          });
        },
        error: err => {
          this.snackBar.open("Înregistrarea a eșuat: " + (err.error?.message || "Eroare necunoscută"), "Închide", {
            duration: 3000,
            panelClass: ['snackbar-error']
          });
        }
      });
  }
  
  /** Încarcă lista de evenimente la care utilizatorul este înregistrat */
  loadRegisteredEvents(): void {
    const userId = this.userID ? Number(this.userID) : null;
  
    if (userId === null || isNaN(userId)) return;
  
    this.eventRegistrationService.getRegisteredEventIds(userId).subscribe({
      next: (ids) => {
        this.registeredEventIds = ids;
      },
      error: (err) => {
        console.error("Nu s-au putut încărca evenimentele înregistrate:", err);
      }
    });
  }
  
  /** Verifică dacă utilizatorul este înregistrat la un anumit eveniment */
  isRegistered(eventId: number): boolean {
    return this.registeredEventIds.includes(eventId);
  }
  
  /** Extrage orașul dintr-o locație textuală */
  extractCityFromLocation(location: string): string {
    if (!location) return "Oraș necunoscut";
    
    const parts = location.split(',').map(p => p.trim());
    if (parts.length >= 2) {
      return parts[parts.length - 2]; 
    }
    return location;
  }
  
  /** Actualizează lista de orașe disponibile pentru filtrare */
  updateCityFilters(): void {
    const cityMap: { [city: string]: number } = {};
  
    this.events.forEach(event => {
      const city = event.cityName;

      if (city) {
        cityMap[city] = (cityMap[city] || 0) + 1;
      }
    });
  
    this.cityCounts = cityMap;
    this.availableCities = Object.keys(cityMap).sort();
  }
  /** Centrează harta pe orașul selectat */
  centerMapOnCity(): void {
  if (!this.selectedCity) return;

  const cityEvents = this.events.filter(
    event => event.cityName === this.selectedCity
  );
  

  if (cityEvents.length > 0) {
    const firstEvent = cityEvents[0];
    this.center = { lat: firstEvent.lat, lng: firstEvent.lng };

    if (this.gmapElement?._googleMap) {
      this.gmapElement._googleMap.panTo(this.center);
      this.gmapElement._googleMap.setZoom(13); 
    }
  }
}
/** Navighează către pagina de chat pentru un eveniment */
goToChat(eventId: number): void {
  this.router.navigate(['/chat', eventId]);
}
/** Verifică dacă utilizatorul are acces la chatul evenimentului */
isChatAvailable(event: any): boolean {
  const userId = Number(sessionStorage.getItem('userId'));
  const role = sessionStorage.getItem('role');

  if (role === 'USER') {
    return this.registeredEventIds.includes(event.id);
  } else if (role === 'ASSOCIATION') {
    return event.organizerId === userId;
  }

  return false;
}
/** Anulează înregistrarea utilizatorului la un eveniment */
cancelRegistration(eventId: number): void {
  const userId = this.userID ? Number(this.userID) : null;

  if (userId === null || isNaN(userId)) {
    this.snackBar.open("ID-ul utilizatorului este invalid. Te rugăm să te loghezi din nou.", "Închide", {
      duration: 3000,
      panelClass: ['snackbar-error']
    });
    return;
  }

  const dialogRef = this.dialog.open(ConfirmDialogComponent, {
    width: '350px',
    data: {
      title: 'Confirmă anularea',
      message: 'Ești sigur că vrei să îți anulezi înregistrarea la acest eveniment?',
      confirmButtonText: 'Anulează înregistrarea'
    }
  });

  dialogRef.afterClosed().subscribe(result => {
    if (result) {
      this.eventRegistrationService.unregisterFromEvent(eventId, userId).subscribe({
        next: () => {
          this.snackBar.open("Înregistrarea a fost anulată cu succes", "Închide", {
            duration: 3000,
            panelClass: ['snackbar-success']
          });
          const index = this.registeredEventIds.indexOf(eventId);
          if (index !== -1) {
            this.registeredEventIds.splice(index, 1);
          }
          this.eventService.getEventById(eventId).subscribe({
            next: (updatedEvent) => {
              const index = this.events.findIndex(e => e.id === eventId);
              if (index !== -1) {
                this.events[index] = updatedEvent;
              }
            }
          });
        },
        error: (err) => {
          this.snackBar.open("Anularea a eșuat: " + (err.error || "Eroare necunoscută"), "Închide", {
            duration: 3000,
            panelClass: ['snackbar-error']
          });
        }
      });
    }
  });
}



/** Șterge evenimentul (doar pentru organizator) */
deleteEvent(eventId: number): void {
  const dialogRef = this.dialog.open(ConfirmDialogComponent, {
    width: '350px',
    data: {
      title: 'Confirmă ștergerea',
      message: 'Ești sigur că vrei să ștergi acest eveniment?',
      confirmButtonText: 'Șterge'
    }
  });

  dialogRef.afterClosed().subscribe(result => {
    if (result) {
      this.eventService.deleteEvent(eventId).subscribe({
        next: () => {
          this.snackBar.open("Evenimentul a fost șters cu succes", "Închide", {
            duration: 3000,
            panelClass: ['snackbar-success']
          });
          this.loadEvents();
        },
        error: (err) => {
          this.snackBar.open("Ștergerea a eșuat: " + (err.error || "Eroare necunoscută"), "Închide", {
            duration: 3000,
            panelClass: ['snackbar-error']
          });
        }
      });
    }
  });
}

/** Verifică dacă utilizatorul este abonat la un eveniment */
isSubscribed(eventId: number): boolean {
  return this.subscribedEventIds.includes(eventId);
}

/** Abonează utilizatorul pentru notificări la un eveniment full */
subscribeToEvent(eventId: number) {
  if (!this.userID) {
    this.snackBar.open("Trebuie să fii autentificat pentru a te abona.", "Închide", {
      duration: 3000,
      panelClass: ['snackbar-error']
    });
    return;
  }

  this.eventService.subscribeToEvent(eventId, Number(this.userID)).subscribe({
    next: () => {
      this.subscribedEventIds.push(eventId);
      this.snackBar.open("Te-ai abonat. Vei fi notificat când se eliberează un loc.", "Închide", {
        duration: 3000,
        panelClass: ['snackbar-success']
      });
    },
    error: err => {
      const message = err?.error?.message || 'Eroare la abonare.';
      this.snackBar.open(message, "Închide", {
        duration: 3000,
        panelClass: ['snackbar-error']
      });
    }
  }); 
}

/** Dezabonează utilizatorul de la notificările pentru un eveniment */
unsubscribeFromEvent(eventId: number): void {
  const userId = Number(this.userID);
  this.eventService.unsubscribeFromEvent(eventId, userId).subscribe({
    next: () => {
      this.subscribedEventIds = this.subscribedEventIds.filter(id => id !== eventId);
      this.snackBar.open("Ai renunțat la notificare pentru acest eveniment.", "Închide", {
        duration: 3000,
        panelClass: ['snackbar-success']
      });
    },
    error: err => {
      this.snackBar.open("Eroare la dezabonare.", "Închide", {
        duration: 3000,
        panelClass: ['snackbar-error']
      });
    }
  });
}

/** Încarcă lista de evenimente la care utilizatorul este abonat */
loadSubscribedEvents(): void {
  const userId = this.userID ? Number(this.userID) : null;
  if (!userId || isNaN(userId)) return;

  this.eventService.getSubscribedEventIds(userId).subscribe({
    next: (ids) => {
      this.subscribedEventIds = ids;
    },
    error: (err) => {
      console.error("Eroare la încărcarea evenimentelor abonate:", err);
    }
  });
}


}