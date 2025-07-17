import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CertificateService {
  private apiUrl = 'http://localhost:8080/api/events';

  constructor(private http: HttpClient) {}

  /** Încarcă o ștampilă pentru un eveniment */
  uploadStamp(eventId: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post(`${this.apiUrl}/${eventId}/upload-stamp`, formData, { withCredentials: true });
  }

  /** Generează certificate pentru participanții unui eveniment */
  generateCertificates(eventId: number, request: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/${eventId}/generate-certificates`, request, { withCredentials: true });
  }
}
