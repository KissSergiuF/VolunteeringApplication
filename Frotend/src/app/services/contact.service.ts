import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ContactService {
  private apiURL = 'http://localhost:8080/contact';

  constructor(private http: HttpClient) {}

  /** Trimite un mesaj din formularul de contact către backend */
  sendContactMessage(contactForm: any): Observable<any> {
    return this.http.post(this.apiURL, contactForm);
  }
}
