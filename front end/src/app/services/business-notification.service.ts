import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, forkJoin, Observable, Subject, tap } from 'rxjs';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { environment } from '../../environments/environment';
import {
  BusinessNotification,
  UnreadNotificationCount
} from '../models/business-notification.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class BusinessNotificationService implements OnDestroy {
  private readonly apiUrl = environment.apiUrl + '/notifications';
  private readonly notificationsSubject = new BehaviorSubject<BusinessNotification[]>([]);
  private readonly unreadCountSubject = new BehaviorSubject<number>(0);
  private readonly latestNotificationSubject = new Subject<BusinessNotification>();
  private readonly connectedSubject = new BehaviorSubject<boolean>(false);

  private client?: Client;
  private subscription?: StompSubscription;

  readonly notifications$ = this.notificationsSubject.asObservable();
  readonly unreadCount$ = this.unreadCountSubject.asObservable();
  readonly latestNotification$ = this.latestNotificationSubject.asObservable();
  readonly connected$ = this.connectedSubject.asObservable();

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  connect(): void {
    const token = this.authService.token;
    if (!token || this.client?.active) {
      return;
    }

    this.refresh();

    this.client = new Client({
      brokerURL: this.webSocketUrl(),
      connectHeaders: {
        Authorization: 'Bearer ' + token
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: () => undefined
    });

    this.client.onConnect = () => {
      this.connectedSubject.next(true);
      this.subscription?.unsubscribe();
      this.subscription = this.client?.subscribe(
        '/user/queue/notifications',
        (message: IMessage) => this.handleIncoming(message)
      );
      this.refresh();
    };

    this.client.onWebSocketClose = () => {
      this.connectedSubject.next(false);
    };

    this.client.onStompError = () => {
      this.connectedSubject.next(false);
    };

    this.client.activate();
  }

  disconnect(): void {
    this.subscription?.unsubscribe();
    this.subscription = undefined;
    if (this.client?.active) {
      void this.client.deactivate();
    }
    this.client = undefined;
    this.connectedSubject.next(false);
    this.notificationsSubject.next([]);
    this.unreadCountSubject.next(0);
  }

  refresh(): void {
    if (!this.authService.token) {
      return;
    }

    forkJoin({
      notifications: this.http.get<BusinessNotification[]>(this.apiUrl),
      unread: this.http.get<UnreadNotificationCount>(this.apiUrl + '/unread-count')
    }).subscribe({
      next: ({ notifications, unread }) => {
        this.notificationsSubject.next(notifications);
        this.unreadCountSubject.next(unread.count);
      }
    });
  }

  markAsRead(notification: BusinessNotification): Observable<BusinessNotification> {
    return this.http.patch<BusinessNotification>(
      this.apiUrl + '/' + notification.id + '/read',
      {}
    ).pipe(
      tap(updated => {
        this.notificationsSubject.next(
          this.notificationsSubject.value.map(item =>
            item.id === updated.id ? updated : item
          )
        );
        if (!notification.read) {
          this.unreadCountSubject.next(
            Math.max(0, this.unreadCountSubject.value - 1)
          );
        }
      })
    );
  }

  markAllAsRead(): Observable<void> {
    return this.http.patch<void>(this.apiUrl + '/read-all', {}).pipe(
      tap(() => {
        this.notificationsSubject.next(
          this.notificationsSubject.value.map(item => ({
            ...item,
            read: true
          }))
        );
        this.unreadCountSubject.next(0);
      })
    );
  }

  ngOnDestroy(): void {
    this.disconnect();
  }

  private handleIncoming(message: IMessage): void {
    try {
      const notification = JSON.parse(message.body) as BusinessNotification;
      const current = this.notificationsSubject.value;
      const alreadyExists = current.some(item => item.id === notification.id);
      this.notificationsSubject.next([
        notification,
        ...current.filter(item => item.id !== notification.id)
      ].slice(0, 50));

      if (!alreadyExists && !notification.read) {
        this.unreadCountSubject.next(this.unreadCountSubject.value + 1);
      }
      this.latestNotificationSubject.next(notification);
    } catch {
      this.refresh();
    }
  }

  private webSocketUrl(): string {
    const backendUrl = environment.apiUrl.replace(/\/api\/?$/, '');
    return backendUrl.replace(/^http/, 'ws') + '/ws-notifications';
  }
}
