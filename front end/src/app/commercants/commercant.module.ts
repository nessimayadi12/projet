import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PermissionsModule } from '../guards/permissions.module';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommercantListComponent } from './commercant-list/commercant-list.component';
import { CommercantDetailComponent } from './commercant-detail/commercant-detail.component';
import { CommercantBasicFormComponent } from './commercant-basic-form/commercant-basic-form.component';

@NgModule({
  declarations: [
    CommercantListComponent,
    CommercantDetailComponent,
    CommercantBasicFormComponent
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
