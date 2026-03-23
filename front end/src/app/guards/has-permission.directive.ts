import { Directive, Input, TemplateRef, ViewContainerRef, OnInit } from '@angular/core';
import { ScreenService } from '../services/screen.service';

@Directive({
  selector: '[appHasPermission]'
})
export class HasPermissionDirective implements OnInit {
  private screenCode: string;
  private permissionType: 'canView' | 'canCreate' | 'canEdit' | 'canDelete' | 'canExport';

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private screenService: ScreenService
  ) { }

  @Input()
  set appHasPermission(value: { screen: string, permission: string }) {
    this.screenCode = value.screen;
    this.permissionType = value.permission as any;
  }

  ngOnInit() {
    this.screenService.hasPermission(this.screenCode, this.permissionType).subscribe(
      hasPermission => {
        if (hasPermission) {
          this.viewContainer.createEmbeddedView(this.templateRef);
        } else {
          this.viewContainer.clear();
        }
      },
      error => {
        // En cas d'erreur, cacher l'élément par sécurité
        this.viewContainer.clear();
      }
    );
  }
}
