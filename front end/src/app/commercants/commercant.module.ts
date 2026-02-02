import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
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
    FormsModule,
    ReactiveFormsModule,
    RouterModule
  ]
})
export class CommercantModule { }
