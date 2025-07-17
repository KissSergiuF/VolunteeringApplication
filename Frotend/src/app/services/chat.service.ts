import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject, Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private stompClient: Client;
  private messageSubject = new Subject<any>();
  private readonly REST_API_URL = 'http://localhost:8080/api/chat';
  private readonly WS_ENDPOINT = 'http://localhost:8080/ws-chat';

  constructor(private http: HttpClient) {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(this.WS_ENDPOINT),
      reconnectDelay: 5000,
      debug: (str) => console.log('[STOMP]', str)
    });
  }

  /** Se conectează la WebSocket și ascultă mesaje pentru un eveniment */
  connect(eventId: number): void {
    if (this.stompClient.active) return;

    this.stompClient.onConnect = () => {
      this.stompClient.subscribe(`/topic/chat/${eventId}`, (message: IMessage) => {
        try {
          const chatMessage = JSON.parse(message.body);
          this.messageSubject.next(chatMessage);
        } catch (e) {
          console.error('Eroare la parsarea mesajului:', e);
        }
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Eroare STOMP:', frame.headers['message'], frame.body);
    };

    this.stompClient.onWebSocketClose = (event) => {
      console.warn('Conexiunea WebSocket a fost închisă:', event.reason);
    };

    this.stompClient.onWebSocketError = (error) => {
      console.error('Eroare WebSocket:', error);
    };

    this.stompClient.activate();
  }

  /** Trimite un mesaj către WebSocket */
  sendMessage(eventId: number, message: any): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.publish({
        destination: `/app/chat/${eventId}`,
        body: JSON.stringify(message)
      });
    } else {
      console.warn('Nu se poate trimite mesajul - conexiune STOMP indisponibilă');
    }
  }

  /** Returnează mesajele primite prin WebSocket */
  getMessages(): Observable<any> {
    return this.messageSubject.asObservable();
  }

  /** Obține istoricul mesajelor pentru un eveniment */
  getChatHistory(eventId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.REST_API_URL}/${eventId}`);
  }

  /** Închide conexiunea WebSocket */
  disconnect(): void {
    if (this.stompClient && this.stompClient.active) {
      this.stompClient.deactivate();
    }
  }
}
