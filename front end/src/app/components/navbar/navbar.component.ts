import { Component, OnInit, OnDestroy, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { BusinessNotificationService } from '../../services/business-notification.service';
import { BusinessNotification } from '../../models/business-notification.model';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
    mobile_menu_visible: any = 0;
    private toggleButton: any;
    private sidebarVisible: boolean;
    private subscriptions = new Subscription();
    private toastTimer?: ReturnType<typeof setTimeout>;

    notifications: BusinessNotification[] = [];
    unreadCount = 0;
    notificationsOpen = false;
    notificationsConnected = false;
    toastNotification: BusinessNotification | null = null;

    constructor(
      private element: ElementRef, 
      private router: Router,
      private authService: AuthService,
      private notificationService: BusinessNotificationService
    ) {
      this.sidebarVisible = false;
    }

    ngOnInit(){
      const navbar: HTMLElement = this.element.nativeElement;
      this.toggleButton = navbar.getElementsByClassName('navbar-toggler')[0];
      this.subscriptions.add(this.router.events.subscribe(() => {
        this.sidebarClose();
        this.notificationsOpen = false;
         var $layer: any = document.getElementsByClassName('close-layer')[0];
         if ($layer) {
           $layer.remove();
           this.mobile_menu_visible = 0;
         }
      }));

      this.subscriptions.add(
        this.notificationService.notifications$.subscribe(notifications => {
          this.notifications = notifications;
        })
      );
      this.subscriptions.add(
        this.notificationService.unreadCount$.subscribe(count => {
          this.unreadCount = count;
        })
      );
      this.subscriptions.add(
        this.notificationService.connected$.subscribe(connected => {
          this.notificationsConnected = connected;
        })
      );
      this.subscriptions.add(
        this.notificationService.latestNotification$.subscribe(notification => {
          this.toastNotification = notification;
          if (this.toastTimer) {
            clearTimeout(this.toastTimer);
          }
          this.toastTimer = setTimeout(() => {
            this.toastNotification = null;
          }, 6000);
        })
      );

      this.notificationService.connect();
    }

    sidebarOpen() {
        const toggleButton = this.toggleButton;
        const body = document.getElementsByTagName('body')[0];
        setTimeout(function(){
            toggleButton.classList.add('toggled');
        }, 500);

        body.classList.add('nav-open');

        this.sidebarVisible = true;
    };
    sidebarClose() {
        const body = document.getElementsByTagName('body')[0];
        this.toggleButton.classList.remove('toggled');
        this.sidebarVisible = false;
        body.classList.remove('nav-open');
    };
    sidebarToggle() {
        // const toggleButton = this.toggleButton;
        // const body = document.getElementsByTagName('body')[0];
        var $toggle = document.getElementsByClassName('navbar-toggler')[0];

        if (this.sidebarVisible === false) {
            this.sidebarOpen();
        } else {
            this.sidebarClose();
        }
        const body = document.getElementsByTagName('body')[0];

        if (this.mobile_menu_visible == 1) {
            // $('html').removeClass('nav-open');
            body.classList.remove('nav-open');
            if ($layer) {
                $layer.remove();
            }
            setTimeout(function() {
                $toggle.classList.remove('toggled');
            }, 400);

            this.mobile_menu_visible = 0;
        } else {
            setTimeout(function() {
                $toggle.classList.add('toggled');
            }, 430);

            var $layer = document.createElement('div');
            $layer.setAttribute('class', 'close-layer');


            if (body.querySelectorAll('.main-panel')) {
                document.getElementsByClassName('main-panel')[0].appendChild($layer);
            }else if (body.classList.contains('off-canvas-sidebar')) {
                document.getElementsByClassName('wrapper-full-page')[0].appendChild($layer);
            }

            setTimeout(function() {
                $layer.classList.add('visible');
            }, 100);

            $layer.onclick = function() { //asign a function
              body.classList.remove('nav-open');
              this.mobile_menu_visible = 0;
              $layer.classList.remove('visible');
              setTimeout(function() {
                  $layer.remove();
                  $toggle.classList.remove('toggled');
              }, 400);
            }.bind(this);

            body.classList.add('nav-open');
            this.mobile_menu_visible = 1;

        }
    };

    logout(): void {
      this.notificationService.disconnect();
      this.authService.logout();
      this.router.navigate(['/login']);
    }

    toggleNotifications(event: MouseEvent): void {
      event.stopPropagation();
      this.notificationsOpen = !this.notificationsOpen;
    }

    openNotification(notification: BusinessNotification): void {
      const navigate = () => {
        this.notificationsOpen = false;
        if (notification.actionUrl) {
          this.router.navigateByUrl(notification.actionUrl);
        }
      };

      if (notification.read) {
        navigate();
        return;
      }

      this.notificationService.markAsRead(notification).subscribe({
        next: navigate,
        error: navigate
      });
    }

    markAllAsRead(event: MouseEvent): void {
      event.stopPropagation();
      if (this.unreadCount === 0) {
        return;
      }
      this.notificationService.markAllAsRead().subscribe();
    }

    dismissToast(): void {
      this.toastNotification = null;
    }

    notificationIcon(type: string): string {
      if (type.includes('PANNE') || type.includes('REPARE') || type.includes('REMPLACE')) {
        return 'build';
      }
      if (type.includes('AFFECTE')) {
        return 'devices';
      }
      return 'assignment';
    }

    ngOnDestroy(): void {
      this.subscriptions.unsubscribe();
      if (this.toastTimer) {
        clearTimeout(this.toastTimer);
      }
    }
}
