import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PermissionsModule } from '../guards/permissions.module';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommercantListComponent } from './commercant-list/commercant-list.component';
import { CommercantFormComponent } from './commercant-form/commercant-form.component';

@NgModule({
  declarations: [
    CommercantListComponent,
    CommercantFormComponent
  ],
  imports: [
    CommonModule,
    PermissionsModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule
  ]
})
export class CommercantModule { }
