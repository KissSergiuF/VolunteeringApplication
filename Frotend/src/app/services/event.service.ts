import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class EventService {
  constructor(private http: HttpClient) {}

  /** Adaugă un nou eveniment în sistem */
  addEvent(eventData: any) {
    return this.http.post('http://localhost:8080/api/events', eventData, {
      withCredentials: true 
    });
  }

  /** Obține toate evenimentele disponibile (neîncheiate) */
  getAvailableEvents() {
    return this.http.get<any[]>('http://localhost:8080/api/events', {
      withCredentials: true
    });
  }

  /** Șterge un eveniment după ID-ul său */
  deleteEvent(eventId: number): Observable<any> {
    return this.http.delete(`http://localhost:8080/api/events/${eventId}`, {
      withCredentials: true
    });
  }

  /** Obține lista de evenimente arhivate */
  getArchivedEvents(): Observable<any[]> {
    return this.http.get<any[]>('http://localhost:8080/api/events/archived', {
      withCredentials: true
    });
  }

  /** Obține lista de participanți activi la un eveniment */
  getParticipants(eventId: number) {
    return this.http.get<any[]>(`http://localhost:8080/api/registrations/${eventId}/participants`);
  }

  /** Obține toți membrii asociați unui eveniment, inclusiv cei eliminați */
  getAllMembers(eventId: number) {
    return this.http.get<any[]>(`http://localhost:8080/api/registrations/${eventId}/all-members`);
  }

  /** Elimină un utilizator dintr-un eveniment */
  kickUserFromEvent(eventId: number, userId: number) {
    return this.http.delete(`http://localhost:8080/api/events/kick/${eventId}/${userId}`, {
      withCredentials: true
    });
  }

  /** Verifică dacă utilizatorul este banat dintr-un anumit eveniment */
  checkIfUserIsBanned(eventId: number): Observable<{ banned: boolean }> {
    return this.http.get<{ banned: boolean }>(
      `http://localhost:8080/api/events/${eventId}/ban-status`,
      { withCredentials: true }
    );
  }

  /** Obține detaliile unui eveniment după ID */
  getEventById(eventId: number): Observable<any> {
    return this.http.get<any>(`http://localhost:8080/api/events/${eventId}`, {
      withCredentials: true
    });
  }

  /** Înregistrează utilizatorul la un eveniment */
  subscribeToEvent(eventId: number, userId: number): Observable<any> {
    return this.http.post(`http://localhost:8080/api/registrations/subscribe`, null, {
      params: { eventId, userId },
      withCredentials: true
    });
  }

  /** Dezînregistrează utilizatorul de la un eveniment */
  unsubscribeFromEvent(eventId: number, userId: number): Observable<any> {
    return this.http.delete(`http://localhost:8080/api/registrations/unsubscribe`, {
      params: { eventId, userId },
      withCredentials: true
    });
  }

  /** Obține lista de ID-uri ale evenimentelor la care utilizatorul este înregistrat */
  getSubscribedEventIds(userId: number): Observable<number[]> {
    return this.http.get<number[]>(`http://localhost:8080/api/registrations/subscribed/${userId}`, {
      withCredentials: true
    });
  }
}
