import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class EventFeedbackService {
  private baseUrl = 'http://localhost:8080/api/feedback';

  constructor(private http: HttpClient) {}

  /** Trimite un feedback general către organizator (ex: de la voluntar către eveniment/asociație) */
  submitFeedback(dto: any) {
    return this.http.post(`${this.baseUrl}`, dto);
  }

  /** Trimite un feedback către un participant specific (ex: de la organizator către voluntar) */
  submitFeedbackToParticipant(participantId: number, dto: any) {
    return this.http.post(`${this.baseUrl}/participant/${participantId}`, dto);
  }

  /** Obține toate feedback-urile primite de un organizator */
  getFeedbackForOrganizer(organizerId: number) {
    return this.http.get<any[]>(`${this.baseUrl}/organizer/${organizerId}`);
  }

  /** Obține toate feedback-urile trimise de un anumit utilizator */
  getFeedbackFromUser(userId: number) {
    return this.http.get<any[]>(`http://localhost:8080/api/feedback/user/${userId}`);
  }
}
