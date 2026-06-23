import { Directive, Input, OnDestroy, TemplateRef, ViewContainerRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ScreenPermissions } from '../models/screen.model';
import { ScreenService } from '../services/screen.service';

@Directive({ selector: '[appHasPermission]' })
export class HasPermissionDirective implements OnDestroy {
  private destroy$ = new Subject<void>();
  private rendered = false;

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private screenService: ScreenService
  ) {}

  @Input()
  set appHasPermission(value: { screen: string; permission: keyof ScreenPermissions }) {
    this.destroy$.next();
    this.screenService.hasPermission(value.screen, value.permission).pipe(
      takeUntil(this.destroy$)
    ).subscribe(hasPermission => this.render(hasPermission));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private render(hasPermission: boolean): void {
    if (hasPermission && !this.rendered) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.rendered = true;
    } else if (!hasPermission && this.rendered) {
      this.viewContainer.clear();
      this.rendered = false;
    }
  }
}