import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PermissionsModule } from '../guards/permissions.module';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TpeListComponent } from './tpe-list/tpe-list.component';
import { TpeFormComponent } from './tpe-form/tpe-form.component';
import { TpeImportRecordsComponent } from './tpe-import-records/tpe-import-records.component';
import { GestionTauxComponent } from './gestion-taux/gestion-taux.component';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';

@NgModule({
  declarations: [
    TpeListComponent,
    TpeFormComponent,
    TpeImportRecordsComponent,
    GestionTauxComponent
  ],
  imports: [
    CommonModule,
    PermissionsModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule
    ,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatTooltipModule,
    MatDialogModule,
    MatSnackBarModule
  ]
})
export class TpeModule { }
