import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({
  providedIn: 'root'
})
export class ExcelExportService {

  constructor() { }

  /**
   * Exporte des données vers un fichier Excel
   * @param data - Tableau de données à exporter
   * @param fileName - Nom du fichier (sans extension)
   * @param sheetName - Nom de la feuille Excel
   */
  exportToExcel(data: any[], fileName: string, sheetName: string = 'Sheet1'): void {
    if (!data || data.length === 0) {
      console.warn('Aucune donnée à exporter');
      return;
    }

    // Créer une nouvelle feuille de calcul
    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(data);

    // Ajuster automatiquement la largeur des colonnes
    const columnWidths = this.calculateColumnWidths(data);
    worksheet['!cols'] = columnWidths;

    // Créer un nouveau classeur et ajouter la feuille
    const workbook: XLSX.WorkBook = {
      Sheets: { [sheetName]: worksheet },
      SheetNames: [sheetName]
    };

    // Générer le fichier Excel et le télécharger
    const excelBuffer: any = XLSX.write(workbook, {
      bookType: 'xlsx',
      type: 'array'
    });

    this.saveAsExcelFile(excelBuffer, fileName);
  }

  /**
   * Exporte plusieurs feuilles vers un seul fichier Excel
   * @param sheets - Tableau d'objets contenant les données et noms des feuilles
   * @param fileName - Nom du fichier (sans extension)
   */
  exportMultipleSheetsToExcel(
    sheets: { data: any[], sheetName: string }[],
    fileName: string
  ): void {
    if (!sheets || sheets.length === 0) {
      console.warn('Aucune donnée à exporter');
      return;
    }

    const workbook: XLSX.WorkBook = {
      Sheets: {},
      SheetNames: []
    };

    sheets.forEach(sheet => {
      if (sheet.data && sheet.data.length > 0) {
        const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(sheet.data);
        const columnWidths = this.calculateColumnWidths(sheet.data);
        worksheet['!cols'] = columnWidths;

        workbook.Sheets[sheet.sheetName] = worksheet;
        workbook.SheetNames.push(sheet.sheetName);
      }
    });

    const excelBuffer: any = XLSX.write(workbook, {
      bookType: 'xlsx',
      type: 'array'
    });

    this.saveAsExcelFile(excelBuffer, fileName);
  }

  /**
   * Calcule automatiquement la largeur des colonnes
   */
  private calculateColumnWidths(data: any[]): any[] {
    if (!data || data.length === 0) return [];

    const keys = Object.keys(data[0]);
    const columnWidths: any[] = [];

    keys.forEach(key => {
      let maxLength = key.length;
      data.forEach(row => {
        const value = row[key];
        if (value) {
          const length = value.toString().length;
          if (length > maxLength) {
            maxLength = length;
          }
        }
      });
      columnWidths.push({ wch: Math.min(maxLength + 2, 50) });
    });

    return columnWidths;
  }

  /**
   * Sauvegarde le buffer Excel en tant que fichier
   */
  private saveAsExcelFile(buffer: any, fileName: string): void {
    const data: Blob = new Blob([buffer], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });

    const link = document.createElement('a');
    const url = URL.createObjectURL(data);
    
    link.href = url;
    link.download = `${fileName}_${this.getFormattedDate()}.xlsx`;
    link.click();

    // Nettoyer
    setTimeout(() => {
      URL.revokeObjectURL(url);
    }, 100);
  }

  /**
   * Retourne la date actuelle formatée
   */
  private getFormattedDate(): string {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');
    
    return `${year}${month}${day}_${hours}${minutes}${seconds}`;
  }
}
