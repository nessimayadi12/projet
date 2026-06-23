import { Component } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AssistantResponseDTO, AssistantMetierRow } from '../../models/assistant-metier.model';
import { AssistantMetierService } from '../../services/assistant-metier.service';
import { ExcelExportService } from '../../services/excel-export.service';

@Component({
  selector: 'app-assistant-metier',
  templateUrl: './assistant-metier.component.html',
  styleUrls: ['./assistant-metier.component.css']
})
export class AssistantMetierComponent {
  question = '';
  loading = false;
  response: AssistantResponseDTO | null = null;
  historiqueQuestions: string[] = [];

  constructor(
    private assistantService: AssistantMetierService,
    private snackBar: MatSnackBar,
    private excelExportService: ExcelExportService
  ) { }

  poserQuestion(): void {
    const cleanQuestion = this.question.trim();
    if (!cleanQuestion) {
      this.showError('Saisir une question avant de lancer l assistant.');
      return;
    }

    this.loading = true;

    this.assistantService.interroger({
      question: cleanQuestion
    }).subscribe({
      next: (response) => {
        this.response = response;
        this.addToHistory(cleanQuestion);
        if (response.erreur) {
          this.showError(response.messageErreur || 'Question hors perimetre de l application TPE.');
        }
        this.loading = false;
      },
      error: (error) => {
        this.showError(this.resolveError(error));
        this.loading = false;
      }
    });
  }

  onQuestionKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.poserQuestion();
    }
  }

  reposerQuestion(question: string): void {
    this.question = question;
    this.poserQuestion();
  }

  reset(): void {
    this.question = '';
    this.response = null;
  }

  hasResults(): boolean {
    return !!this.response && !!this.response.donnees && this.response.donnees.length > 0;
  }

  getColumns(): string[] {
    const firstRow = this.response && this.response.donnees && this.response.donnees.length > 0
      ? this.response.donnees[0]
      : null;
    return firstRow ? Object.keys(firstRow) : [];
  }

  displayValue(row: AssistantMetierRow, column: string): string {
    const value = row[column];
    if (value === null || value === undefined || value === '') {
      return '-';
    }

    if (typeof value === 'object') {
      return JSON.stringify(value);
    }

    return String(value);
  }

  exporterExcel(): void {
    if (!this.hasResults() || !this.response) {
      this.showError('Aucune donnee a exporter.');
      return;
    }
    this.excelExportService.exportToExcel(this.response.donnees, 'assistant_ia_resultats', 'Assistant IA');
  }

  private addToHistory(question: string): void {
    this.historiqueQuestions = [
      question,
      ...this.historiqueQuestions.filter(item => item !== question)
    ].slice(0, 8);
  }

  private resolveError(error: any): string {
    if (error && error.error && error.error.messageErreur) {
      return error.error.messageErreur;
    }
    if (error && error.error && error.error.message) {
      return error.error.message;
    }
    if (error && error.message) {
      return error.message;
    }
    return 'Impossible de contacter l assistant IA.';
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 5000,
      horizontalPosition: 'right',
      verticalPosition: 'top',
      panelClass: ['assistant-snackbar']
    });
  }
}
