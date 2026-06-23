import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PermissionsModule } from '../guards/permissions.module';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatRadioModule } from '@angular/material/radio';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { DemandeListComponent } from './demande-list/demande-list.component';
import { DemandeFormComponent } from './demande-form/demande-form.component';
import { AffectationTPEComponent } from './affectation-tpe/affectation-tpe.component';
import { DemandeValidationComponent } from './demande-validation/demande-validation.component';

@NgModule({
  declarations: [
    DemandeListComponent,
    DemandeFormComponent,
    AffectationTPEComponent,
    DemandeValidationComponent
  ],
  imports: [
    CommonModule,
    PermissionsModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    MatButtonModule,
    MatDialogModule,
    MatSnackBarModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatRadioModule,
    MatProgressSpinnerModule
  ],
  exports: [
    DemandeListComponent,
    DemandeFormComponent,
    AffectationTPEComponent
  ]
})
export class DemandesModule { }
