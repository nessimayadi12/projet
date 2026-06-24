import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DiagnosticIaPanne, Panne, StatutPanne, TypePanne } from '../../models/panne.model';
import { StatutTPE } from '../../models/tpe.model';
import { PanneService } from '../../services/panne.service';
import { AuthService } from '../../services/auth.service';
import { TpeService } from '../../services/tpe.service';
import { Role } from '../../models/utilisateur.model';

type WorkflowAction = 'DETAIL' | 'DIAGNOSTIC' | 'RESOLUTION' | 'IRRECUPERABLE';

@Component({
  selector: 'app-panne-list',
  templateUrl: './panne-list.component.html',
  styleUrls: ['./panne-list.component.css']
})
export class PanneListComponent implements OnInit {
  readonly StatutPanne = StatutPanne;

  pannes: Panne[] = [];
  pannesFiltrees: Panne[] = [];
  tpes: any[] = [];
  tpesFiltrees: any[] = [];
  tpeSearchTerm = '';
  loading = false;
  savingAction = false;
  exportingExcel = false;
  exportingPdf = false;
  showDeclarationForm = false;
  analyseIaLoading = false;
  diagnosticIa: DiagnosticIaPanne | null = null;

  declarationForm: FormGroup;
  workflowForm: FormGroup;
  selectedPanne: Panne | null = null;
  workflowAction: WorkflowAction = 'DETAIL';

  filtreStatut = 'TOUS';
  recherchePanne = '';
  dateDebut: string = '';
  dateFin: string = '';

  statuts = [
    'TOUS',
    StatutPanne.DECLAREE,
    StatutPanne.DIAGNOSTIQUEE,
    StatutPanne.EN_REPARATION,
    StatutPanne.REPAREE,
    StatutPanne.IRRECUPERABLE
  ];

  panneTypes = [
    { value: TypePanne.COURT_CIRCUIT, label: 'Le court-circuit' },
    { value: TypePanne.DEFAUT_ISOLEMENT, label: "Le defaut d'isolement" },
    { value: TypePanne.SURCHARGE, label: 'La surcharge' },
    { value: TypePanne.COUPURE_SECTEUR, label: 'La coupure secteur' },
    { value: TypePanne.RUPTURE_USURE_PIECES, label: 'Rupture ou usure de pieces' },
    { value: TypePanne.DEFAUT_LUBRIFICATION, label: 'Defaut de lubrification' },
    { value: TypePanne.GRIPPAGE, label: 'Grippage' },
    { value: TypePanne.HARDWARE, label: 'Materielles (Hardware)' },
    { value: TypePanne.SOFTWARE, label: 'Logicielles (Software)' },
    { value: TypePanne.INCIDENT_0044_0088, label: 'Incident 0044 / 0088' },
    { value: TypePanne.INCIDENT_0060_CENTRE_BANCAIRE_NON_ATTEINT, label: 'Incident 0060 / Centre bancaire non atteint' },
    { value: TypePanne.INCIDENT_001, label: 'Incident 001' },
    { value: TypePanne.INCIDENT_0074, label: 'Incident 0074' },
    { value: TypePanne.INCIDENT_020E_0067, label: 'Incident 020E / 0067' },
    { value: TypePanne.INCIDENT_0050, label: 'Incident 0050' },
    { value: TypePanne.ALERTE_IRRUPTION, label: 'Alerte irruption' },
    { value: TypePanne.PROBLEME_BATTERIE_CHARGE, label: 'Probleme de batterie / charge' },
    { value: TypePanne.IMPRIMANTE_BLOQUEE, label: 'Imprimante bloquee' },
    { value: TypePanne.INCIDENT_0060_ERREUR_CARTE, label: 'Incident 0060 / Erreur carte' },
    { value: TypePanne.ERREUR_SAISIE_PIN, label: 'Erreur saisie PIN' }
  ];

  constructor(
    private fb: FormBuilder,
    private panneService: PanneService,
    private tpeService: TpeService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {
    this.declarationForm = this.fb.group({
      tpeId: ['', Validators.required],
      typePanne: [''],
      description: ['', [Validators.minLength(5)]]
    }, { validators: this.requireTypeOrDescription });

    this.workflowForm = this.fb.group({
      diagnostic: [''],
      solution: [''],
      nouveauNumeroSerie: [''],
      nouveauTypeTPE: [''],
      nouvelleMarque: [''],
      nouveauModele: [''],
      commentaire: [''],
      confirmationIrrecuperable: [false]
    });
  }

  ngOnInit(): void {
    this.loadTPEs();
    this.loadPannes();
  }

  loadTPEs(): void {
    this.tpeService.getAllTPE().subscribe({
      next: (data) => {
        this.tpes = data.filter(tpe => this.isTPEEligibleForDeclaration(tpe.statut));
        this.appliquerRechercheTPE();
        this.clearSelectedTpeIfHidden();
      },
      error: (error) => {
        console.error('Erreur chargement TPEs', error);
        this.showNotification('Erreur lors du chargement des TPE', 'error');
      }
    });
  }

  loadPannes(): void {
    this.loading = true;
    this.panneService.getAllPannes().subscribe({
      next: (data) => {
        this.pannes = this.sortPannes(data || []);
        this.enrichirPannesAvecTPE();
      },
      error: (error) => {
        console.error('Erreur chargement pannes', error);
        this.showNotification(this.getErrorMessage(error, 'Erreur lors du chargement des pannes'), 'error');
        this.loading = false;
      }
    });
  }

  private enrichirPannesAvecTPE(): void {
    if (this.pannes.length === 0) {
      this.appliquerFiltres();
      this.loading = false;
      return;
    }

    this.tpeService.getAllTPE().subscribe({
      next: (tpes) => {
        this.pannes.forEach(panne => {
          const tpe = tpes.find(t => Number(t.id) === Number(panne.tpeId));
          if (tpe) {
            panne.tpeNumeroSerie = panne.tpeNumeroSerie || tpe.numeroSerie;
            panne.commercantNom = panne.commercantNom || tpe.commercantActuelNom || tpe.commercantNom || 'Non affecte';
          }
        });
        this.appliquerFiltres();
        this.loading = false;
      },
      error: () => {
        this.appliquerFiltres();
        this.loading = false;
      }
    });
  }

  appliquerFiltres(): void {
    this.pannesFiltrees = this.sortPannes(this.pannes.filter(panne => {
      const matchRecherche = this.matchesPanneSearch(panne);
      const matchStatut = this.filtreStatut === 'TOUS' || panne.statut === this.filtreStatut;
      const matchDate = this.matchesPanneDateRange(panne);
      return matchRecherche && matchStatut && matchDate;
    }));
  }

  clearRecherchePanne(): void {
    this.recherchePanne = '';
    this.appliquerFiltres();
  }

  private matchesPanneSearch(panne: Panne): boolean {
    const query = this.normalizeSearchValue(this.recherchePanne);
    if (!query) {
      return true;
    }

    const searchableValues = [
      panne.reference,
      panne.tpeNumeroSerie,
      panne.tpeId,
      panne.commercantNom,
      panne.typePanne,
      this.getTypePanneLabel(panne.typePanne),
      panne.description,
      panne.statut,
      this.getStatutLabel(panne.statut),
      panne.diagnostic,
      panne.solution,
      panne.declarantNom,
      panne.technicienNom
    ];

    return searchableValues.some(value =>
      this.normalizeSearchValue(value).includes(query)
    );
  }

  private normalizeSearchValue(value: unknown): string {
    return String(value ?? '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, ' ')
      .trim();
  }

  private matchesPanneDateRange(panne: Panne): boolean {
    if (!this.dateDebut && !this.dateFin) {
      return true;
    }

    const value = panne.dateDeclaration || panne.createdAt || panne.createdDate;
    if (!value) {
      return false;
    }

    const timestamp = new Date(value).getTime();
    if (Number.isNaN(timestamp)) {
      return false;
    }

    const start = this.dateDebut ? this.localDateBoundary(this.dateDebut, false) : null;
    const end = this.dateFin ? this.localDateBoundary(this.dateFin, true) : null;
    return (start === null || timestamp >= start) && (end === null || timestamp <= end);
  }

  private localDateBoundary(value: string, endOfDay: boolean): number {
    const [year, month, day] = value.split('-').map(Number);
    return new Date(
      year,
      month - 1,
      day,
      endOfDay ? 23 : 0,
      endOfDay ? 59 : 0,
      endOfDay ? 59 : 0,
      endOfDay ? 999 : 0
    ).getTime();
  }

  onFiltreChange(): void {
    this.appliquerFiltres();
  }

  toggleDeclarationForm(): void {
    if (!this.canDeclarerPanne()) {
      this.showNotification('Seule une agence ou un admin peut declarer une panne', 'error');
      return;
    }

    this.showDeclarationForm = !this.showDeclarationForm;
    if (!this.showDeclarationForm) {
      this.declarationForm.reset();
      this.clearTpeSearch();
      this.resetDiagnosticIa();
    }
  }

  declarerPanne(): void {
    if (!this.canDeclarerPanne()) {
      this.showNotification('Seule une agence ou un admin peut declarer une panne', 'error');
      return;
    }

    if (this.declarationForm.invalid) {
      this.declarationForm.markAllAsTouched();
      this.showNotification('Renseignez le TPE et au moins un type ou une description', 'error');
      return;
    }

    const selectedTpe = this.tpes.find(tpe => Number(tpe.id) === Number(this.declarationForm.value.tpeId));
    if (selectedTpe && !this.isTPEEligibleForDeclaration(selectedTpe.statut)) {
      this.showNotification('Le TPE doit etre affecte, en panne ou en maintenance', 'error');
      return;
    }

    const panneData = {
      tpeId: Number(this.declarationForm.value.tpeId),
      description: this.cleanText(this.declarationForm.value.description),
      typePanne: this.declarationForm.value.typePanne || null,
      statut: StatutPanne.DECLAREE
    };

    this.panneService.declarerPanne(panneData as any).subscribe({
      next: () => {
        this.showNotification('Panne declaree avec succes', 'success');
        this.declarationForm.reset();
        this.resetDiagnosticIa();
        this.showDeclarationForm = false;
        this.loadTPEs();
        this.loadPannes();
      },
      error: (error) => {
        console.error('Erreur declaration panne', error);
        this.showNotification(this.getErrorMessage(error, 'Erreur lors de la declaration'), 'error');
      }
    });
  }

  analyserDescriptionIa(): void {
    const description = this.cleanText(this.declarationForm.value.description);
    if (description.length < 5) {
      this.showNotification('Saisissez une description plus precise avant l analyse IA', 'error');
      return;
    }

    this.analyseIaLoading = true;
    this.diagnosticIa = null;
    this.panneService.analyserDiagnosticIa(description).subscribe({
      next: (suggestion) => {
        if (this.cleanText(this.declarationForm.value.description) !== description) {
          this.analyseIaLoading = false;
          return;
        }
        this.diagnosticIa = suggestion;
        this.analyseIaLoading = false;
        if (suggestion.typePanneSuggere) {
          this.declarationForm.patchValue({ typePanne: suggestion.typePanneSuggere });
        }
        this.showNotification('Proposition IA generee', 'success');
      },
      error: (error) => {
        console.error('Erreur analyse IA panne', error);
        this.analyseIaLoading = false;
        this.showNotification(this.getErrorMessage(error, 'Erreur lors de l analyse IA'), 'error');
      }
    });
  }

  appliquerDiagnosticIa(): void {
    if (!this.diagnosticIa?.typePanneSuggere) {
      this.showNotification('Aucun type de panne propose', 'info');
      return;
    }

    this.declarationForm.patchValue({ typePanne: this.diagnosticIa.typePanneSuggere });
    this.showNotification('Type de panne applique', 'success');
  }

  onDescriptionChange(): void {
    this.diagnosticIa = null;
  }

  canAnalyseDescriptionIa(): boolean {
    return this.cleanText(this.declarationForm.value.description).length >= 5 && !this.analyseIaLoading;
  }

  afficherAideTPE(): void {
    if (this.tpes.length === 0) {
      alert('Aucun TPE eligible. Le TPE doit deja etre AFFECTE, EN_PANNE ou MAINTENANCE.');
      return;
    }

    const source = this.tpeSearchTerm ? this.tpesFiltrees : this.tpes;
    if (source.length === 0) {
      alert('Aucun TPE ne correspond a la recherche.');
      return;
    }

    const lines = source.slice(0, 10).map(tpe => this.getTpeDisplayLabel(tpe));
    alert(`TPE eligibles disponibles:\n\n${lines.join('\n')}`);
  }

  onTpeSearchChange(value: string): void {
    this.tpeSearchTerm = value || '';
    this.appliquerRechercheTPE();
    this.clearSelectedTpeIfHidden();
  }

  clearTpeSearch(): void {
    this.tpeSearchTerm = '';
    this.appliquerRechercheTPE();
  }

  getTpeDisplayLabel(tpe: any): string {
    if (!tpe) {
      return '';
    }

    const parts = [
      tpe.numeroSerie || `TPE #${tpe.id}`,
      tpe.marque || '-',
      tpe.modele || '',
      tpe.numeroTerminal ? `TID ${tpe.numeroTerminal}` : '',
      tpe.commercantActuelNom || tpe.commercantNom || '',
      `(${tpe.statut})`
    ];

    return parts.filter(part => String(part).trim()).join(' ');
  }

  ouvrirDetails(panne: Panne): void {
    this.openWorkflowPanel(panne, 'DETAIL');
  }

  diagnostiquerPanne(panne: Panne): void {
    if (!this.ensureTransitionAllowed(panne, StatutPanne.DECLAREE)) {
      return;
    }
    this.openWorkflowPanel(panne, 'DIAGNOSTIC');
  }

  demarrerReparation(panne: Panne): void {
    if (!this.ensureTransitionAllowed(panne, StatutPanne.DIAGNOSTIQUEE)) {
      return;
    }

    this.savingAction = true;
    this.panneService.marquerEnReparation(panne.id!).subscribe({
      next: () => this.afterWorkflowSuccess('Panne marquee en reparation'),
      error: (error) => this.afterWorkflowError(error, 'Erreur lors du demarrage de la reparation')
    });
  }

  resoudrePanne(panne: Panne): void {
    if (!this.ensureTransitionAllowed(panne, StatutPanne.EN_REPARATION)) {
      return;
    }
    this.openWorkflowPanel(panne, 'RESOLUTION');
  }

  marquerIrrecuperable(panne: Panne): void {
    if (!this.ensureTransitionAllowed(panne, StatutPanne.EN_REPARATION)) {
      return;
    }
    this.openWorkflowPanel(panne, 'IRRECUPERABLE');
  }

  submitWorkflowAction(): void {
    if (!this.selectedPanne?.id) {
      return;
    }

    if (this.workflowAction === 'DIAGNOSTIC') {
      const diagnostic = this.cleanText(this.workflowForm.value.diagnostic);
      if (!diagnostic) {
        this.showNotification('Le diagnostic est obligatoire', 'error');
        return;
      }

      this.savingAction = true;
      this.panneService.diagnostiquer(this.selectedPanne.id, diagnostic).subscribe({
        next: () => this.afterWorkflowSuccess('Diagnostic enregistre avec succes'),
        error: (error) => this.afterWorkflowError(error, 'Erreur lors du diagnostic')
      });
      return;
    }

    if (this.workflowAction === 'RESOLUTION') {
      const solution = this.cleanText(this.workflowForm.value.solution);
      if (!solution) {
        this.showNotification('La solution est obligatoire', 'error');
        return;
      }

      this.savingAction = true;
      this.panneService.marquerReparee(this.selectedPanne.id, solution).subscribe({
        next: () => this.afterWorkflowSuccess('Panne resolue avec succes'),
        error: (error) => this.afterWorkflowError(error, 'Erreur lors de la resolution')
      });
      return;
    }

    if (this.workflowAction === 'IRRECUPERABLE') {
      const nouveauNumeroSerie = this.cleanText(this.workflowForm.value.nouveauNumeroSerie);
      const nouveauTypeTPE = this.cleanText(this.workflowForm.value.nouveauTypeTPE);
      const nouvelleMarque = this.cleanText(this.workflowForm.value.nouvelleMarque);
      const nouveauModele = this.cleanText(this.workflowForm.value.nouveauModele);
      const commentaire = this.cleanText(this.workflowForm.value.commentaire);
      const confirmation = Boolean(this.workflowForm.value.confirmationIrrecuperable);

      if (!nouveauNumeroSerie) {
        this.showNotification('Le nouveau numero de serie est obligatoire', 'error');
        return;
      }
      if (!nouveauTypeTPE) {
        this.showNotification('Le type du nouveau TPE est obligatoire', 'error');
        return;
      }
      if (!nouvelleMarque) {
        this.showNotification('La marque du nouveau TPE est obligatoire', 'error');
        return;
      }
      if (!nouveauModele) {
        this.showNotification('Le modele du nouveau TPE est obligatoire', 'error');
        return;
      }

      if (!confirmation) {
        this.showNotification('Confirmez le remplacement du TPE avant de valider', 'error');
        return;
      }

      this.savingAction = true;
      this.panneService
        .marquerIrrecuperableAvecRemplacement(
          this.selectedPanne.id,
          nouveauNumeroSerie,
          nouveauTypeTPE,
          nouvelleMarque,
          nouveauModele,
          commentaire
        )
        .subscribe({
          next: () => this.afterWorkflowSuccess('TPE marque irrecuperable et remplace'),
          error: (error) => this.afterWorkflowError(error, 'Erreur lors du remplacement du TPE')
        });
    }
  }

  closeWorkflowPanel(): void {
    this.selectedPanne = null;
    this.workflowAction = 'DETAIL';
    this.workflowForm.reset();
  }

  canDeclarerPanne(): boolean {
    return this.authService.hasAnyRole([Role.ADMIN, Role.AGENCE]);
  }

  canTraiterPanne(): boolean {
    return this.authService.hasAnyRole([Role.ADMIN, Role.MONETIQUE]);
  }

  canShowDeclarationHint(): boolean {
    return this.canDeclarerPanne() && this.tpes.length === 0;
  }

  getTypePanneLabel(typePanne?: TypePanne | string): string {
    const match = this.panneTypes.find(item => item.value === typePanne);
    return match ? match.label : (typePanne || '-');
  }

  getStatutLabel(statut?: string): string {
    const labels: { [key: string]: string } = {
      DECLAREE: 'Declaree',
      DIAGNOSTIQUEE: 'Diagnostiquee',
      EN_REPARATION: 'En reparation',
      REPAREE: 'Reparee / resolue',
      TESTEE: 'Testee',
      IRRECUPERABLE: 'Irrecuperable'
    };
    return statut ? labels[statut] || statut : '-';
  }

  getStatutClass(statut: string): string {
    const classes: { [key: string]: string } = {
      DECLAREE: 'badge-warning',
      DIAGNOSTIQUEE: 'badge-info',
      EN_REPARATION: 'badge-primary',
      REPAREE: 'badge-success',
      TESTEE: 'badge-success',
      IRRECUPERABLE: 'badge-secondary'
    };
    return classes[statut] || 'badge-default';
  }

  getUrgenceIaClass(urgence?: string): string {
    const classes: { [key: string]: string } = {
      FAIBLE: 'ia-urgence-low',
      MOYENNE: 'ia-urgence-medium',
      HAUTE: 'ia-urgence-high',
      CRITIQUE: 'ia-urgence-critical'
    };
    return urgence ? classes[urgence] || 'ia-urgence-medium' : 'ia-urgence-medium';
  }

  getDiagnosticAgence(): string {
    const diagnostic = this.cleanText(this.diagnosticIa?.diagnosticPropose);
    if (!diagnostic) {
      return 'Aucun diagnostic fiable pour le moment. Completez la description avec le message affiche et les tests deja effectues.';
    }

    return diagnostic
      .replace(/^Selon la base de connaissances RAG, le cas le plus proche est "[^"]+"\.\s*/i, '')
      .replace(/\s*Sources retenues:.*$/i, '')
      .trim();
  }

  getActionsAgence(): string[] {
    const recommandations = this.diagnosticIa?.recommandations || [];
    if (recommandations.length > 0) {
      return recommandations.slice(0, 4);
    }

    const action = this.cleanText(this.diagnosticIa?.actionCorrectiveProposee);
    if (action) {
      return action
        .split(/[.;]/)
        .map(item => item.trim())
        .filter(item => item.length > 0)
        .slice(0, 4);
    }

    return [
      'Verifier le message affiche sur le TPE',
      'Redemarrer le terminal si cela ne presente pas de risque',
      'Noter les tests deja effectues avant declaration'
    ];
  }

  getInformationsATransmettre(): string[] {
    return [
      'Message exact affiche sur le TPE',
      'Moment ou la panne est apparue',
      'Tests deja effectues par l agence',
      'Impact client: paiement bloque, ticket non imprime ou TPE hors service'
    ];
  }

  getStatCount(statut: StatutPanne): number {
    return this.pannes.filter(panne => panne.statut === statut).length;
  }

  getEnCoursCount(): number {
    return this.pannes.filter(panne =>
      [StatutPanne.DECLAREE, StatutPanne.DIAGNOSTIQUEE, StatutPanne.EN_REPARATION].includes(panne.statut)
    ).length;
  }

  getNextActionLabel(panne: Panne): string {
    if (!this.canTraiterPanne()) {
      return 'Consulter';
    }
    if (panne.statut === StatutPanne.DECLAREE) {
      return 'Diagnostiquer';
    }
    if (panne.statut === StatutPanne.DIAGNOSTIQUEE) {
      return 'Demarrer';
    }
    if (panne.statut === StatutPanne.EN_REPARATION) {
      return 'Finaliser';
    }
    return 'Consulter';
  }

  exportToExcel(): void {
    this.exportingExcel = true;
    this.panneService.exportRapportPannes().subscribe({
      next: (blob) => {
        this.saveBlob(blob, 'pannes_tpe.xlsx');
        this.showNotification('Export Excel genere avec succes', 'success');
        this.exportingExcel = false;
      },
      error: (error) => {
        this.exportingExcel = false;
        this.showNotification(this.getErrorMessage(error, 'Erreur lors de l export Excel'), 'error');
      }
    });
  }

  exportToPDF(): void {
    this.exportingPdf = true;
    this.panneService.exportRapportPannesPdf().subscribe({
      next: (blob) => {
        this.saveBlob(blob, 'pannes_tpe.pdf');
        this.showNotification('Export PDF genere avec succes', 'success');
        this.exportingPdf = false;
      },
      error: (error) => {
        this.exportingPdf = false;
        this.showNotification(this.getErrorMessage(error, 'Erreur lors de l export PDF'), 'error');
      }
    });
  }

  private openWorkflowPanel(panne: Panne, action: WorkflowAction): void {
    this.selectedPanne = panne;
    this.workflowAction = action;
    this.workflowForm.reset({
      diagnostic: panne.diagnostic || '',
      solution: panne.actionCorrective || panne.solution || '',
      nouveauNumeroSerie: '',
      nouveauTypeTPE: '',
      nouvelleMarque: '',
      nouveauModele: '',
      commentaire: panne.commentaireTechnicien || '',
      confirmationIrrecuperable: false
    });
  }

  isIrrecuperableReady(): boolean {
    return Boolean(
      this.cleanText(this.workflowForm.value.nouveauNumeroSerie)
      && this.cleanText(this.workflowForm.value.nouveauTypeTPE)
      && this.cleanText(this.workflowForm.value.nouvelleMarque)
      && this.cleanText(this.workflowForm.value.nouveauModele)
      && this.workflowForm.value.confirmationIrrecuperable
    );
  }

  private ensureTransitionAllowed(panne: Panne, expectedStatut: StatutPanne): boolean {
    if (!this.canTraiterPanne()) {
      this.showNotification('Seul Monetique ou Admin peut traiter une panne', 'error');
      return false;
    }

    if (panne.statut !== expectedStatut) {
      this.showNotification(`Action autorisee uniquement depuis ${expectedStatut}`, 'error');
      return false;
    }

    return true;
  }

  private afterWorkflowSuccess(message: string): void {
    this.savingAction = false;
    this.showNotification(message, 'success');
    this.closeWorkflowPanel();
    this.loadTPEs();
    this.loadPannes();
  }

  private afterWorkflowError(error: any, fallback: string): void {
    console.error(fallback, error);
    this.savingAction = false;
    this.showNotification(this.getErrorMessage(error, fallback), 'error');
  }

  private requireTypeOrDescription(group: AbstractControl): ValidationErrors | null {
    const typePanne = group.get('typePanne')?.value as string | undefined;
    const description = group.get('description')?.value as string | undefined;
    return (typePanne && typePanne.trim()) || (description && description.trim())
      ? null
      : { typeOrDescriptionRequired: true };
  }

  private isTPEEligibleForDeclaration(statut: StatutTPE | string): boolean {
    return [
      StatutTPE.AFFECTE,
      StatutTPE.EN_PANNE,
      StatutTPE.MAINTENANCE,
      'EN_MAINTENANCE'
    ].includes(statut as any);
  }

  private appliquerRechercheTPE(): void {
    const term = this.normalizeSearch(this.tpeSearchTerm);

    if (!term) {
      this.tpesFiltrees = [...this.tpes];
      return;
    }

    this.tpesFiltrees = this.tpes.filter(tpe => this.normalizeSearch([
      tpe.id,
      tpe.numeroSerie,
      tpe.serieTpe,
      tpe.numeroTerminal,
      tpe.marque,
      tpe.modele,
      tpe.typeTpe,
      tpe.typeTPE,
      tpe.statut,
      tpe.commercantActuelNom,
      tpe.commercantNom,
      tpe.raisonSociale,
      tpe.numeroAffiliation
    ].join(' ')).includes(term));
  }

  private clearSelectedTpeIfHidden(): void {
    const selectedTpeId = this.declarationForm.get('tpeId')?.value;

    if (!selectedTpeId) {
      return;
    }

    const selectedTpeIsVisible = this.tpesFiltrees.some(tpe =>
      Number(tpe.id) === Number(selectedTpeId)
    );

    if (!selectedTpeIsVisible) {
      this.declarationForm.patchValue({ tpeId: '' });
    }
  }

  private normalizeSearch(value: unknown): string {
    return String(value || '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim();
  }

  private sortPannes(pannes: Panne[]): Panne[] {
    return [...pannes].sort((a, b) => {
      const dateA = a.dateDeclaration ? new Date(a.dateDeclaration).getTime() : 0;
      const dateB = b.dateDeclaration ? new Date(b.dateDeclaration).getTime() : 0;
      return dateB - dateA;
    });
  }

  private cleanText(value: unknown): string {
    return typeof value === 'string' ? value.trim() : '';
  }

  private resetDiagnosticIa(): void {
    this.diagnosticIa = null;
    this.analyseIaLoading = false;
  }

  private saveBlob(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  private showNotification(message: string, type: 'success' | 'error' | 'info'): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: [`snackbar-${type}`]
    });
  }

  private getErrorMessage(error: any, fallback: string): string {
    if (typeof error?.error === 'string') {
      return error.error;
    }
    return error?.error?.message || error?.message || fallback;
  }
}
