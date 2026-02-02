import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TpeListComponent } from './tpe-list/tpe-list.component';
import { TpeFormComponent } from './tpe-form/tpe-form.component';

@NgModule({
  declarations: [
    TpeListComponent,
    TpeFormComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule
  ]
})
export class TpeModule { }
