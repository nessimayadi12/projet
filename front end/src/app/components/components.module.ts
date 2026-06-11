import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { FooterComponent } from './footer/footer.component';
import { NavbarComponent } from './navbar/navbar.component';
import { SidebarComponent } from './sidebar/sidebar.component';
import { PowerbiPublicReportComponent } from './powerbi-public-report/powerbi-public-report.component';
import { UploadFichierBancaireComponent } from './upload-fichier-bancaire/upload-fichier-bancaire.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatButtonModule,
    MatProgressBarModule
  ],
  declarations: [
    FooterComponent,
    NavbarComponent,
    SidebarComponent,
    PowerbiPublicReportComponent,
    UploadFichierBancaireComponent
  ],
  exports: [
    FooterComponent,
    NavbarComponent,
    SidebarComponent,
    PowerbiPublicReportComponent,
    UploadFichierBancaireComponent
  ]
})
export class ComponentsModule { }
