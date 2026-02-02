import { Routes } from '@angular/router';

import { DashboardComponent } from '../../dashboard/dashboard.component';
import { UserProfileComponent } from '../../user-profile/user-profile.component';
import { TpeListComponent } from '../../tpe/tpe-list/tpe-list.component';
import { TpeFormComponent } from '../../tpe/tpe-form/tpe-form.component';
import { CommercantListComponent } from '../../commercants/commercant-list/commercant-list.component';
import { CommercantFormComponent } from '../../commercants/commercant-form/commercant-form.component';
import { DemandeListComponent } from '../../demandes/demande-list/demande-list.component';
import { DemandeFormComponent } from '../../demandes/demande-form/demande-form.component';
import { AffectationTPEComponent } from '../../demandes/affectation-tpe/affectation-tpe.component';
import { PanneListComponent } from '../../maintenance/panne-list/panne-list.component';
import { AuthGuard } from '../../guards/auth.guard';
import { Role } from '../../models/utilisateur.model';

export const AdminLayoutRoutes: Routes = [
    { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard], data: { roles: [Role.ADMIN, Role.MONETIQUE] } },
    { path: 'user-profile', component: UserProfileComponent, canActivate: [AuthGuard] },
    
    // Routes TPE
    { path: 'tpe', component: TpeListComponent, canActivate: [AuthGuard] },
    { path: 'tpe/new', component: TpeFormComponent, canActivate: [AuthGuard], data: { roles: [Role.ADMIN, Role.LOGISTIQUE] } },
    { path: 'tpe/:id/edit', component: TpeFormComponent, canActivate: [AuthGuard], data: { roles: [Role.ADMIN, Role.LOGISTIQUE] } },
    { path: 'tpe/:id', component: TpeFormComponent, canActivate: [AuthGuard] },
    
    // Routes Commerçants
    { path: 'commercants', component: CommercantListComponent, canActivate: [AuthGuard] },
    { path: 'commercants/new', component: CommercantFormComponent, canActivate: [AuthGuard], data: { roles: [Role.ADMIN, Role.AGENCE, Role.COMMERCANT] } },
    { path: 'commercants/:id/edit', component: CommercantFormComponent, canActivate: [AuthGuard], data: { roles: [Role.ADMIN, Role.AGENCE, Role.COMMERCANT] } },
    { path: 'commercants/:id', component: CommercantFormComponent, canActivate: [AuthGuard] },
    
    // Routes Demandes
    { path: 'demandes', component: DemandeListComponent, canActivate: [AuthGuard] },
    { path: 'demandes/new', component: DemandeFormComponent, canActivate: [AuthGuard], data: { roles: [Role.ADMIN, Role.AGENCE] } },
    { path: 'demandes/:id/edit', component: DemandeFormComponent, canActivate: [AuthGuard], data: { roles: [Role.ADMIN, Role.AGENCE] } },
    { path: 'demandes/:id/affecter', component: AffectationTPEComponent, canActivate: [AuthGuard], data: { roles: [Role.ADMIN, Role.MONETIQUE] } },
    { path: 'demandes/:id', component: DemandeFormComponent, canActivate: [AuthGuard] },
    
    // Routes Maintenance/Pannes
    { path: 'pannes', component: PanneListComponent, canActivate: [AuthGuard], data: { roles: [Role.ADMIN, Role.MONETIQUE, Role.TECHNICIEN, Role.AGENCE] } }
];
