import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Feedback } from '../models/feedback.model'; 

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private baseUrl = 'http://localhost:8080/user';

  constructor(private http: HttpClient) {}

  /** Obține datele profilului utilizatorului logat */
  getProfile(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/profil`, {
      withCredentials: true
    });
  }

  /** Trimite un request pentru actualizarea profilului utilizatorului */
  updateProfile(formData: FormData): Observable<any> {
    return this.http.put(`${this.baseUrl}/update`, formData, {
      withCredentials: true,
      responseType: 'text' 
    });
  }

  /** Obține lista de feedback-uri primite de utilizatorul specificat */
  getFeedbackForUser(userId: number): Observable<Feedback[]> {
    return this.http.get<Feedback[]>(`http://localhost:8080/api/feedback/user/${userId}`);
  }

  /** Obține media ratingurilor primite de un utilizator */
  getAverageRatingForUser(userId: number): Observable<number> {
    return this.http.get<number>(`http://localhost:8080/api/feedback/user/${userId}/average`);
  }

  /** Obține numărul total de feedback-uri primite de un utilizator */
  getFeedbackCountForUser(userId: number): Observable<number> {
    return this.http.get<number>(`http://localhost:8080/api/feedback/user/${userId}/count`);
  }

  /** Obține datele publice ale unui utilizator după ID */
  getUserById(userId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/users/${userId}`);
  }
}
