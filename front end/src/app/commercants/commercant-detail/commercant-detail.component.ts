import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Commercant } from '../../models/commercant.model';
import { StatutTPE, TPE } from '../../models/tpe.model';
import { CommercantService } from '../../services/commercant.service';
import { TpeService } from '../../services/tpe.service';

@Component({
  selector: 'app-commercant-detail',
  templateUrl: './commercant-detail.component.html',
  styleUrls: ['./commercant-detail.component.css']
})
export class CommercantDetailComponent implements OnInit {
  commercant: Commercant | null = null;
  tpes: TPE[] = [];
  loadingCommercant = true;
  loadingTpes = true;
  error: string | null = null;
  tpeError: string | null = null;
  expandedTpeId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private commercantService: CommercantService,
    private tpeService: TpeService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!id) {
      this.error = 'Identifiant du commer\u00e7ant invalide.';
      this.loadingCommercant = false;
      this.loadingTpes = false;
      return;
    }

    this.loadCommercant(id);
    this.loadTpes(id);
  }

  loadCommercant(id: number): void {
    this.commercantService.getCommercantById(id).subscribe({
      next: commercant => {
        this.commercant = commercant;
        this.loadingCommercant = false;
      },
      error: err => {
        console.error('Erreur lors du chargement du commer\u00e7ant:', err);
        this.error = 'Impossible de charger les d\u00e9tails du commer\u00e7ant.';
        this.loadingCommercant = false;
      }
    });
  }

  loadTpes(commercantId: number): void {
    this.tpeService.getTPEByCommercant(commercantId).subscribe({
      next: tpes => {
        this.tpes = Array.isArray(tpes) ? tpes : [];
        this.loadingTpes = false;
      },
      error: err => {
        console.error('Erreur lors du chargement des TPE du commer\u00e7ant:', err);
        this.tpeError = 'Impossible de charger les TPE affect\u00e9s \u00e0 ce commer\u00e7ant.';
        this.loadingTpes = false;
      }
    });
  }

  toggleTpeDetails(tpe: TPE): void {
    if (!tpe.id) {
      return;
    }
    this.expandedTpeId = this.expandedTpeId === tpe.id ? null : tpe.id;
  }

  isExpanded(tpe: TPE): boolean {
    return !!tpe.id && this.expandedTpeId === tpe.id;
  }

  editCommercant(): void {
    if (this.commercant?.id) {
      this.router.navigate(['/commercants', this.commercant.id, 'edit']);
    }
  }

  viewTpe(tpe: TPE): void {
    if (tpe.id) {
      this.router.navigate(['/tpe', tpe.id]);
    }
  }

  goBack(): void {
    this.router.navigate(['/commercants']);
  }

  getTpeType(tpe: TPE): string {
    return tpe.typeTPE || tpe.typeTpe || '-';
  }

  getStatutLabel(statut: StatutTPE): string {
    return (statut || '').replace(/_/g, ' ');
  }

  getStatutClass(statut: StatutTPE): string {
    switch (statut) {
      case StatutTPE.DISPONIBLE: return 'status-success';
      case StatutTPE.AFFECTE: return 'status-primary';
      case StatutTPE.EN_PANNE: return 'status-danger';
      case StatutTPE.MAINTENANCE: return 'status-warning';
      case StatutTPE.HORS_SERVICE: return 'status-dark';
      case StatutTPE.RESERVE: return 'status-info';
      default: return 'status-muted';
    }
  }

  display(value: unknown): string {
    return value === null || value === undefined || value === '' ? '-' : String(value);
  }
}
