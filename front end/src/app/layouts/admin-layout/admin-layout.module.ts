import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AdminLayoutRoutes } from './admin-layout.routing';
import { DashboardComponent } from '../../dashboard/dashboard.component';
import { DashboardTpeComponent } from '../../dashboard/dashboard-tpe/dashboard-tpe.component';
import { DashboardDemandesComponent } from '../../dashboard/dashboard-demandes/dashboard-demandes.component';
import { DashboardPannesComponent } from '../../dashboard/dashboard-pannes/dashboard-pannes.component';
import { UserProfileComponent } from '../../user-profile/user-profile.component';
import { ScreenManagementComponent } from '../../components/screen-management/screen-management.component';
import { HasPermissionDirective } from '../../guards/has-permission.directive';
import { TpeModule } from '../../tpe/tpe.module';
import { CommercantModule } from '../../commercants/commercant.module';
import { DemandesModule } from '../../demandes/demandes.module';
import { MaintenanceModule } from '../../maintenance/maintenance.module';
import {MatButtonModule} from '@angular/material/button';
import {MatInputModule} from '@angular/material/input';
import {MatRippleModule} from '@angular/material/core';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatSelectModule} from '@angular/material/select';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatIconModule} from '@angular/material/icon';

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(AdminLayoutRoutes),
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatRippleModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTooltipModule,
    MatDialogModule,
    MatSnackBarModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatIconModule,
    TpeModule,
    CommercantModule,
    DemandesModule,
    MaintenanceModule
  ],
  declarations: [
    DashboardComponent,
    DashboardTpeComponent,
    DashboardDemandesComponent,
    DashboardPannesComponent,
    UserProfileComponent,
    ScreenManagementComponent,
    HasPermissionDirective
  ]
})

export class AdminLayoutModule {}
