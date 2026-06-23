export type NotificationPriority = 'MOYENNE' | 'HAUTE' | 'CRITIQUE' | string;

export interface BusinessNotification {
  id: number;
  type: string;
  title: string;
  message: string;
  priority: NotificationPriority;
  sourceUsername?: string;
  sourceCodeAgence?: string;
  actionUrl?: string;
  read: boolean;
  createdAt: string;
  readAt?: string;
}

export interface UnreadNotificationCount {
  count: number;
}
