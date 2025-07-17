import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = 'http://localhost:8080/auth';

  constructor(private http: HttpClient) {}

  /** Autentifică utilizatorul și returnează datele contului */
  login(email: string, password: string): Observable<{ userId: string; role: string, firstName: string, lastName: string, profilePicture?: string }> {
    return this.http.post<{ userId: string; role: string, firstName: string, lastName: string, profilePicture?: string }>(
      `${this.baseUrl}/login`,
      { email, password },
      { withCredentials: true }
    );
  }

  /** Înregistrează un utilizator nou în sistem */
  register(user: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    telephone: string;
    role: string;
  }): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.baseUrl}/register`,
      user 
    );
  }

  /** Deloghează utilizatorul curent */
  logout(): Observable<string> {
    return this.http.get<string>(
      `${this.baseUrl}/logout`,
      { withCredentials: true } 
    );
  }
}
