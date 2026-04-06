import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TpeService, TPEImportRecord } from '../../services/tpe.service';

@Component({
  selector: 'app-tpe-import-records',
  templateUrl: './tpe-import-records.component.html',
  styleUrls: ['./tpe-import-records.component.css']
})
export class TpeImportRecordsComponent implements OnInit {
  records: TPEImportRecord[] = [];
  loading = false;
  error: string | null = null;
  page = 0;
  size = 50;
  totalElements = 0;
  totalPages = 0;
  pageSizeOptions = [25, 50, 100];

  constructor(
    private tpeService: TpeService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadRecords();
  }

  loadRecords(): void {
    this.loading = true;
    this.error = null;

    this.tpeService.getImportRecords(this.page, this.size).subscribe({
      next: (response) => {
        this.records = response.content || [];
        this.totalElements = response.totalElements || 0;
        this.totalPages = response.totalPages || 0;
        this.size = response.size || this.size;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des lignes importées:', err);
        this.error = 'Impossible de charger les lignes importées';
        this.loading = false;
      }
    });
  }

  onPageSizeChange(value: string): void {
    this.size = Number(value);
    this.page = 0;
    this.loadRecords();
  }

  nextPage(): void {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.loadRecords();
    }
  }

  previousPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadRecords();
    }
  }

  goToTpeList(): void {
    this.router.navigate(['/tpe']);
  }
}
