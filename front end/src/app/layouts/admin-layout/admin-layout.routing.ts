import { Routes } from '@angular/router';

import { DashboardComponent } from '../../dashboard/dashboard.component';
import { DashboardTpeComponent } from '../../dashboard/dashboard-tpe/dashboard-tpe.component';
import { DashboardDemandesComponent } from '../../dashboard/dashboard-demandes/dashboard-demandes.component';
import { DashboardPannesComponent } from '../../dashboard/dashboard-pannes/dashboard-pannes.component';
import { UserProfileComponent } from '../../user-profile/user-profile.component';
import { TpeListComponent } from '../../tpe/tpe-list/tpe-list.component';
import { TpeFormComponent } from '../../tpe/tpe-form/tpe-form.component';
import { TpeImportRecordsComponent } from '../../tpe/tpe-import-records/tpe-import-records.component';
import { GestionTauxComponent } from '../../tpe/gestion-taux/gestion-taux.component';
import { CommercantListComponent } from '../../commercants/commercant-list/commercant-list.component';
import { CommercantFormComponent } from '../../commercants/commercant-form/commercant-form.component';
import { DemandeListComponent } from '../../demandes/demande-list/demande-list.component';
import { DemandeFormComponent } from '../../demandes/demande-form/demande-form.component';
import { AffectationTPEComponent } from '../../demandes/affectation-tpe/affectation-tpe.component';
import { PanneListComponent } from '../../maintenance/panne-list/panne-list.component';
import { ScreenManagementComponent } from '../../components/screen-management/screen-management.component';
import { FileUploadComponent } from '../../components/file-upload/file-upload.component';
import { UploadFichierBancaireComponent } from '../../components/upload-fichier-bancaire/upload-fichier-bancaire.component';
import { AuthGuard } from '../../guards/auth.guard';

export const AdminLayoutRoutes: Routes = [
    { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard], data: { screenCode: 'DASHBOARD' } },
    { path: 'dashboard/tpe', component: DashboardTpeComponent, canActivate: [AuthGuard], data: { screenCode: 'DASHBOARD_TPE' } },
    { path: 'dashboard/demandes', component: DashboardDemandesComponent, canActivate: [AuthGuard], data: { screenCode: 'DASHBOARD_DEMANDES' } },
    { path: 'dashboard/pannes', component: DashboardPannesComponent, canActivate: [AuthGuard], data: { screenCode: 'DASHBOARD_PANNES' } },
    { path: 'user-profile', component: UserProfileComponent, canActivate: [AuthGuard], data: { screenCode: 'PROFIL_UTILISATEUR' } },
    
    // Routes TPE
    { path: 'tpe', component: TpeListComponent, canActivate: [AuthGuard], data: { screenCode: 'LISTE_TPE' } },
    { path: 'tpe/imports', component: TpeImportRecordsComponent, canActivate: [AuthGuard], data: { screenCode: 'LISTE_TPE' } },
    { path: 'tpe/new', component: TpeFormComponent, canActivate: [AuthGuard], data: { screenCode: 'CREER_TPE' } },
    { path: 'tpe/:id/edit', component: TpeFormComponent, canActivate: [AuthGuard], data: { screenCode: 'MODIFIER_TPE' } },
    { path: 'tpe/:id', component: TpeFormComponent, canActivate: [AuthGuard], data: { screenCode: 'DETAIL_TPE' } },

    // Routes Taux
    { path: 'taux', component: GestionTauxComponent, canActivate: [AuthGuard], data: { screenCode: 'GESTION_TAUX' } },
    
    // Routes Commerçants
    { path: 'commercants', component: CommercantListComponent, canActivate: [AuthGuard], data: { screenCode: 'LISTE_COMMERCANTS' } },
    { path: 'commercants/new', component: CommercantFormComponent, canActivate: [AuthGuard], data: { screenCode: 'CREER_COMMERCANT' } },
    { path: 'commercants/:id/edit', component: CommercantFormComponent, canActivate: [AuthGuard], data: { screenCode: 'MODIFIER_COMMERCANT' } },
    { path: 'commercants/:id', component: CommercantFormComponent, canActivate: [AuthGuard], data: { screenCode: 'DETAIL_COMMERCANT' } },
    
    // Routes Demandes
    { path: 'demandes', component: DemandeListComponent, canActivate: [AuthGuard], data: { screenCode: 'LISTE_DEMANDES' } },
    { path: 'demandes/new', component: DemandeFormComponent, canActivate: [AuthGuard], data: { screenCode: 'CREER_DEMANDE' } },
    { path: 'demandes/:id/edit', component: DemandeFormComponent, canActivate: [AuthGuard], data: { screenCode: 'MODIFIER_DEMANDE' } },
    { path: 'demandes/:id/affecter', component: AffectationTPEComponent, canActivate: [AuthGuard], data: { screenCode: 'AFFECTER_TPE' } },
    { path: 'demandes/:id', component: DemandeFormComponent, canActivate: [AuthGuard], data: { screenCode: 'DETAIL_DEMANDE' } },
    
    // Routes Maintenance/Pannes
    { path: 'pannes', component: PanneListComponent, canActivate: [AuthGuard], data: { screenCode: 'LISTE_PANNES' } },
    
    // Route Upload de fichier bancaire (utilise toujours le backend)
    { path: 'file-upload', component: UploadFichierBancaireComponent, canActivate: [AuthGuard], data: { screenCode: 'UPLOAD_FICHIER_BANCAIRE' } },
    
    // Route Administration
    { path: 'admin/screens', component: ScreenManagementComponent, canActivate: [AuthGuard], data: { screenCode: 'GESTION_PERMISSIONS' } }
];
