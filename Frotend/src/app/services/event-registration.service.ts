import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class EventRegistrationService {

  private apiUrl = 'http://localhost:8080/api/registrations';

  constructor(private http: HttpClient) {}

  /** Înregistrează un utilizator la un eveniment */
  registerToEvent(eventId: number, userId: number): Observable<any> {
    const params = new HttpParams()
      .set('eventId', eventId.toString())
      .set('userId', userId.toString());

    return this.http.post(`${this.apiUrl}`, null, { params });
  }

  /** Obține lista de ID-uri ale evenimentelor la care utilizatorul este înregistrat */
  getRegisteredEventIds(userId: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/user/${userId}`);
  }

  /** Șterge înregistrarea unui utilizator de la un eveniment */
  unregisterFromEvent(eventId: number, userId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}?eventId=${eventId}&userId=${userId}`);
  }
}
