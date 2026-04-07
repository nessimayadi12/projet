import { Component, OnInit } from '@angular/core';
import { FileUploadService } from '../../services/file-upload.service';
import { TPEPostingService, TPEInfo, PorteurInfo, EcritureComptable } from '../../services/tpe-posting.service';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { saveAs } from 'file-saver';
import { firstValueFrom } from 'rxjs';

interface ProcessingResult {
  totalLines: number;
  processedLines: number;
  errorLines: number;
  transactions: BankTransactionRecord[];
  errors: ErrorDetail[];
  header?: BankFileHeader;
}

interface BankFileHeader {
  recordType: string;
  bankCode: string;
  date: string;
  sequenceNumber: string;
  rawContent: string;
}

interface BankTransactionRecord {
  lineNumber: number;
  recordType: string; // "10" ou "20"
  nAffiliation: string; // Position 16, 10 caractères
  narrative: string; // Position 50, 25 caractères
  numeroCarte?: string; // Position 113, 16 caractères (type 20 seulement)
  indicateur?: string; // Position 99, 1 caractère (type 20: T ou I)
  montant: string; // Position 215 ou 242 selon type
  commission?: string; // Position 219 (type 10)
  dateTransaction: string;
  rawContent: string;
  // Écritures comptables simulées
  ecrituresComptables: EcritureComptable[];
}

interface ErrorDetail {
  lineNumber: number;
  content: string;
  error: string;
}

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.css']
})
export class FileUploadComponent implements OnInit {
  selectedFile: File | null = null;
  uploading = false;
  uploadSuccess = false;
  uploadError = false;
  message = '';
  processingResult: ProcessingResult | null = null;
  useBackend = true; // Mode backend activé par défaut
  saveToDatabase = false; // Option pour sauvegarder dans la base
  backendProcessed = false; // Indique si le traitement a été fait par le backend
  lastSessionDate: string = ''; // Dernière date de session traitée

  constructor(
    private fileUploadService: FileUploadService,
    private tpePostingService: TPEPostingService
  ) { }

  ngOnInit(): void {
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.uploadSuccess = false;
      this.uploadError = false;
      this.message = '';
    }
  }

  onUpload(): void {
    if (!this.selectedFile) {
      this.uploadError = true;
      this.message = 'Veuillez sélectionner un fichier';
      return;
    }

    this.uploading = true;
    this.uploadSuccess = false;
    this.uploadError = false;
    this.processingResult = null;

    // Si mode backend activé, utiliser l'API backend
    if (this.useBackend) {
      this.uploadToBackend();
    } else {
      // Sinon, lecture et parsing du fichier localement
      const reader = new FileReader();
      reader.onload = async (e: any) => {
        try {
          const content = e.target.result;
          this.processingResult = await this.parseFixedWidthFile(content);
          
          this.uploading = false;
          this.uploadSuccess = true;
          this.message = `Fichier traité avec succès: ${this.processingResult.processedLines} lignes sur ${this.processingResult.totalLines} lignes`;
          
          // Option pour sauvegarder dans la base de données
          if (this.saveToDatabase && this.processingResult.transactions.length > 0) {
            await this.saveToBackend();
          }
        } catch (error: any) {
          this.uploading = false;
          this.uploadError = true;
          this.message = 'Erreur lors du traitement du fichier: ' + error.message;
        }
      };
      
      reader.onerror = () => {
        this.uploading = false;
        this.uploadError = true;
        this.message = 'Erreur lors de la lecture du fichier';
      };
      
      reader.readAsText(this.selectedFile, 'UTF-8');
    }
  }

  /**
   * Upload le fichier vers le backend pour traitement
   */
  async uploadToBackend(): Promise<void> {
    if (!this.selectedFile) return;

    try {
      const sessionDate = this.getSessionDate();
      this.lastSessionDate = sessionDate;
      
      const result = await firstValueFrom(
        this.tpePostingService.uploadFichierBancaire(this.selectedFile, sessionDate)
      );

      if (result.success) {
        this.uploading = false;
        this.uploadSuccess = true;
        this.backendProcessed = true;
        this.message = result.message;
        
        // Construire un résultat compatible pour l'affichage
        this.processingResult = {
          totalLines: result.lignesLues,
          processedLines: result.lignesLues,
          errorLines: 0,
          transactions: [], // Le backend ne renvoie pas les détails des transactions
          errors: []
        };
      } else {
        this.uploading = false;
        this.uploadError = true;
        this.backendProcessed = false;
        this.message = result.message;
      }
    } catch (error: any) {
      this.uploading = false;
      this.uploadError = true;
      this.backendProcessed = false;
      this.message = 'Erreur lors du traitement du fichier: ' + (error.error?.message || error.message);
    }
  }

  /**
   * Télécharge le rapport PDF généré par le backend
   */
  async downloadBackendPDFReport(): Promise<void> {
    if (!this.lastSessionDate) return;

    try {
      await firstValueFrom(this.tpePostingService.telechargerRapportPDF(this.lastSessionDate));
    } catch (error: any) {
      this.uploadError = true;
      this.message = 'Erreur lors du téléchargement du rapport PDF: ' + (error.error?.message || error.message);
    }
  }

  /**
   * Télécharge le rapport texte généré par le backend
   */
  async downloadBackendTextReport(): Promise<void> {
    if (!this.lastSessionDate) return;

    try {
      await firstValueFrom(this.tpePostingService.telechargerRapportTexte(this.lastSessionDate));
    } catch (error: any) {
      this.uploadError = true;
      this.message = 'Erreur lors du téléchargement du rapport texte: ' + (error.error?.message || error.message);
    }
  }

  async parseFixedWidthFile(content: string): Promise<ProcessingResult> {
    const lines = content.split('\n').filter(line => line.trim().length > 0);
    const transactions: BankTransactionRecord[] = [];
    const errors: ErrorDetail[] = [];
    let header: BankFileHeader | undefined;
    
    for (let index = 0; index < lines.length; index++) {
      const line = lines[index];
      try {
        const recordType = line.substring(0, 2);
        
        if (recordType === '01') {
          // Ligne d'en-tête
          header = {
            recordType: '01',
            bankCode: line.substring(2, 10).trim(),
            date: this.formatDate(line.substring(10, 18).trim()),
            sequenceNumber: line.substring(18, 28).trim(),
            rawContent: line
          };
        } else if (recordType === '10') {
          // Type 10: Transaction TPE/Commerçant
          const nAffiliation = line.substring(16, 26).trim();
          
          // VERIFICATION : Le TPE doit exister
          const tpeInfo = await this.verifyTPE(nAffiliation);
          
          if (!tpeInfo.exists) {
            // TPE inexistant → on ignore cette ligne
            console.warn(`TPE ${nAffiliation} non trouvé - ligne ${index + 1} ignorée`);
            continue;
          }
          
          const narrative = (tpeInfo.raisonSociale || line.substring(50, 75).trim()).trim();
          const montantBrut = line.substring(242, 254).trim(); // Position 242, 12 caractères
          const commission = line.substring(219, 231).trim(); // Position 219, 12 caractères
          
          // Générer les 4 écritures comptables pour type 10
          const sessionDate = this.getSessionDate();
          const seqNo = line.substring(2, 10).trim(); // Position 2-10
          
          const ecritures: EcritureComptable[] = [
            {
              branch: '999',
              profitCentre: 'TR',
              clientId: '910234', // Viendrait de TPE.N_compte.substring(5,6)
              accountNo: '150.1103.0000',
              accountName: this.getAccountName('150.1103.0000'),
              accountType: 'S',
              ccy: '',
              seqNo: seqNo,
              referenceNo: nAffiliation,
              rbTranType: '',
              valueDate: sessionDate,
              amount: this.formatAmount(montantBrut),
              dc: 'D',
              narrative: narrative,
              tranType: '',
              rbGl: '',
              sessionDate: sessionDate
            },
            {
              branch: '999',
              profitCentre: 'TR',
              clientId: '910234',
              accountNo: '151.1105.0000',
              accountName: this.getAccountName('151.1105.0000'),
              accountType: 'S',
              ccy: '',
              seqNo: seqNo,
              referenceNo: nAffiliation,
              rbTranType: '',
              valueDate: sessionDate,
              amount: this.formatAmount(montantBrut),
              dc: 'C',
              narrative: narrative,
              tranType: '',
              rbGl: '',
              sessionDate: sessionDate
            },
            {
              branch: '999',
              profitCentre: 'TR',
              clientId: '910234',
              accountNo: '601.9106.0000',
              accountName: this.getAccountName('601.9106.0000'),
              accountType: 'S',
              ccy: '',
              seqNo: seqNo,
              referenceNo: nAffiliation,
              rbTranType: '',
              valueDate: sessionDate,
              amount: this.formatAmountCommission(commission),
              dc: 'D',
              narrative: narrative,
              tranType: '',
              rbGl: '',
              sessionDate: sessionDate
            },
            {
              branch: '999',
              profitCentre: 'TR',
              clientId: '910234',
              accountNo: '150.1103.0000',
              accountName: this.getAccountName('150.1103.0000'),
              accountType: 'S',
              ccy: '',
              seqNo: seqNo,
              referenceNo: nAffiliation,
              rbTranType: '',
              valueDate: sessionDate,
              amount: this.formatAmountCommission(commission),
              dc: 'C',
              narrative: narrative,
              tranType: '',
              rbGl: '',
              sessionDate: sessionDate
            }
          ];
          
          transactions.push({
            lineNumber: index + 1,
            recordType: '10',
            nAffiliation: nAffiliation,
            narrative: narrative,
            montant: this.formatAmount(montantBrut),
            commission: this.formatAmountCommission(commission),
            dateTransaction: this.getSessionDate(),
            rawContent: line,
            ecrituresComptables: ecritures
          });
          
        } else if (recordType === '20') {
          // Type 20: Transaction Porteur/Carte
          const indicateur = line.substring(99, 100).trim();
          
          // Seulement si indicateur = 'T' ou 'I'
          if (indicateur === 'T' || indicateur === 'I') {
            const nAffiliation = line.substring(16, 26).trim();
            
            // VERIFICATION 1 : Le TPE doit exister
            const tpeInfo = await this.verifyTPE(nAffiliation);
            if (!tpeInfo.exists) {
              console.warn(`TPE ${nAffiliation} non trouvé - ligne ${index + 1} ignorée`);
              continue;
            }
            
            const numeroCarte = line.substring(113, 129).trim(); // Position 113, 16 caractères
            
            // VERIFICATION 2 : La carte doit exister
            const porteurInfo = await this.verifyPorteur(numeroCarte);
            if (!porteurInfo.exists) {
              console.warn(`Carte ${numeroCarte} non trouvée - ligne ${index + 1} ignorée`);
              continue;
            }
            
            const narrative = (tpeInfo.raisonSociale || line.substring(50, 75).trim()).trim();
            const montant = line.substring(215, 227).trim(); // Position 215, 12 caractères
            const ref = line.substring(209, 215).trim(); // Position 209, 6 caractères
            const tranDate = line.substring(203, 209).trim(); // Position 203, 6 caractères
            
            // Format date: AAMMJJ -> YYYYMMDD
            const formattedDate = '20' + tranDate.substring(4, 6) + tranDate.substring(2, 4) + tranDate.substring(0, 2);
            
            const sessionDate = this.getSessionDate();
            const seqNo = line.substring(2, 10).trim();
            const montantNum = parseFloat(montant) / 1000;
            
            // LOGIQUE CONDITIONNELLE selon la devise
            let ecritures: EcritureComptable[] = [];
            
            // CAS A : Devise locale (TND ou TNC) → 2 ÉCRITURES
            if (porteurInfo.devise === 'TND' || porteurInfo.devise === 'TNC') {
              // Écriture 1 : Débit compte porteur
              ecritures.push({
                branch: porteurInfo.compte.split('-')[0],
                profitCentre: porteurInfo.compte.split('-')[1],
                clientId: porteurInfo.compte.split('-')[2],
                accountNo: porteurInfo.compte.split('-')[3],
                accountName: this.getAccountName(porteurInfo.compte.split('-')[3]),
                accountType: 'S',
                ccy: '',
                seqNo: seqNo,
                referenceNo: ref,
                rbTranType: '',
                valueDate: formattedDate,
                amount: montantNum.toFixed(3),
                dc: 'D',
                narrative: narrative,
                tranType: 'CMS2',
                rbGl: 'C',
                sessionDate: sessionDate
              });
              
              // Écriture 2 : Crédit compte compensation
              ecritures.push({
                branch: '999',
                profitCentre: 'TR',
                clientId: '910234',
                accountNo: '150.1103.0000',
                accountName: this.getAccountName('150.1103.0000'),
                accountType: 'S',
                ccy: '',
                seqNo: seqNo,
                referenceNo: ref,
                rbTranType: '',
                valueDate: sessionDate,
                amount: montantNum.toFixed(3),
                dc: 'C',
                narrative: narrative,
                tranType: '',
                rbGl: '',
                sessionDate: sessionDate
              });
              
            } else {
              // CAS B : Devise étrangère → 4 ÉCRITURES avec conversion
              
              // Convertir le montant en devise locale
              const montantConverti = Math.round((montantNum * porteurInfo.ccyRate) * Math.pow(10, porteurInfo.deciPlaces)) / Math.pow(10, porteurInfo.deciPlaces);
              
              // Écriture 1 : Crédit compte en devises
              ecritures.push({
                branch: '999',
                profitCentre: 'TR',
                clientId: '910234',
                accountNo: '151.1103.0000',
                accountName: this.getAccountName('151.1103.0000'),
                accountType: 'S',
                ccy: porteurInfo.devise,
                seqNo: seqNo,
                referenceNo: ref,
                rbTranType: porteurInfo.devise + ' 1',
                valueDate: formattedDate,
                amount: montantNum.toFixed(porteurInfo.deciPlaces),
                dc: 'C',
                narrative: narrative,
                tranType: '',
                rbGl: '',
                sessionDate: sessionDate
              });
              
              // Écriture 2 : Débit compte change (342)
              ecritures.push({
                branch: '999',
                profitCentre: 'TR',
                clientId: '910234',
                accountNo: '342.1101.0' + porteurInfo.ccyId.substring(0, 2),
                accountName: this.getAccountName('342.1101.0' + porteurInfo.ccyId.substring(0, 2)),
                accountType: 'S',
                ccy: porteurInfo.devise,
                seqNo: seqNo,
                referenceNo: ref,
                rbTranType: porteurInfo.devise + ' 1',
                valueDate: formattedDate,
                amount: montantNum.toFixed(porteurInfo.deciPlaces),
                dc: 'D',
                narrative: narrative,
                tranType: '',
                rbGl: '',
                sessionDate: sessionDate
              });
              
              // Écriture 3 : Débit client (montant converti en TND)
              ecritures.push({
                branch: porteurInfo.compte.split('-')[0],
                profitCentre: porteurInfo.compte.split('-')[1],
                clientId: porteurInfo.compte.split('-')[2],
                accountNo: porteurInfo.compte.split('-')[3],
                accountName: this.getAccountName(porteurInfo.compte.split('-')[3]),
                accountType: 'S',
                ccy: '',
                seqNo: seqNo,
                referenceNo: ref,
                rbTranType: '',
                valueDate: formattedDate,
                amount: montantConverti.toFixed(3),
                dc: 'D',
                narrative: narrative,
                tranType: 'CMS2',
                rbGl: 'C',
                sessionDate: sessionDate
              });
              
              // Écriture 4 : Crédit contrepartie change (341)
              ecritures.push({
                branch: '999',
                profitCentre: 'TR',
                clientId: '910234',
                accountNo: '341.1101.0000',
                accountName: this.getAccountName('341.1101.0000'),
                accountType: 'S',
                ccy: '',
                seqNo: seqNo,
                referenceNo: ref,
                rbTranType: '',
                valueDate: formattedDate,
                amount: montantConverti.toFixed(3),
                dc: 'C',
                narrative: narrative,
                tranType: '',
                rbGl: '',
                sessionDate: sessionDate
              });
            }
            
            transactions.push({
              lineNumber: index + 1,
              recordType: '20',
              nAffiliation: nAffiliation,
              narrative: narrative,
              numeroCarte: numeroCarte,
              indicateur: indicateur,
              montant: this.formatAmount(montant),
              dateTransaction: formattedDate,
              rawContent: line,
              ecrituresComptables: ecritures
            });
          }
        }
      } catch (error: any) {
        errors.push({
          lineNumber: index + 1,
          content: line,
          error: error.message || 'Erreur de parsing'
        });
      }
    }
    
    return {
      totalLines: lines.length,
      processedLines: transactions.length,
      errorLines: errors.length,
      transactions: transactions,
      errors: errors,
      header: header
    };
  }

  formatDate(dateStr: string): string {
    // Format: JJMMAAAA ou AAAAMMJJ
    if (dateStr.length === 8) {
      // Tentative JJMMAAAA
      const day = dateStr.substring(0, 2);
      const month = dateStr.substring(2, 4);
      const year = dateStr.substring(4, 8);
      return `${day}/${month}/${year}`;
    }
    return dateStr;
  }

  formatAmount(amountStr: string): string {
    // Convertir montant: ex: 000000012548 -> 12.548
    // Division par 1000 comme dans le code C#
    const amount = parseInt(amountStr || '0');
    return (amount / 1000).toFixed(3);
  }

  formatAmountCommission(amountStr: string): string {
    // Convertir commission: division par 10000 comme dans le code C#
    const amount = parseInt(amountStr || '0');
    return (amount / 10000).toFixed(4);
  }

  getSessionDate(): string {
    // Format YYYYMMDD comme SESSIONDATE dans le code C#
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}${month}${day}`;
  }

  getAccountName(accountNo: string): string {
    // Retourne le nom du compte selon le numéro
    const accountNames: { [key: string]: string } = {
      '150.1103.0000': 'Compte de compensation mon Nq',
      '151.1103.0000': 'Compte devises étrangères',
      '151.1105.0000': 'COMMISSION SUR RETRAIT GAB ABC',
      '150.1206.0000': 'GIOSEPPO PORTEURS VISA NATIONAL',
      '601.9106.0000': 'BILLETS ET MONNAIES CAIS',
      '701.1033.0000': 'COMMISSION D INTERCHANGE',
      '901.1033.0000': 'BILLETS ET MONNAIES DAB',
      '355.1201.0000': 'ETAT TVA COLLECTEE',
      '342.1101.0000': 'Position de change',
      '341.1101.0000': 'Contrepartie change'
    };
    
    // Si le compte commence par un pattern connu
    if (accountNo.startsWith('150.1206')) return 'GIOSEPPO PORTEURS VISA NATIONAL';
    if (accountNo.startsWith('150.1103')) return 'Compte de compensation mon Nq';
    if (accountNo.startsWith('151.1103')) return 'Compte devises étrangères';
    if (accountNo.startsWith('151.1105')) return 'COMMISSION SUR RETRAIT GAB ABC';
    if (accountNo.startsWith('601.9106')) return 'BILLETS ET MONNAIES CAIS';
    if (accountNo.startsWith('701.1033')) return 'COMMISSION D INTERCHANGE';
    if (accountNo.startsWith('901.1033')) return 'BILLETS ET MONNAIES DAB';
    if (accountNo.startsWith('355.1201')) return 'ETAT TVA COLLECTEE';
    if (accountNo.startsWith('342.1101')) return 'Position de change';
    if (accountNo.startsWith('341.1101')) return 'Contrepartie change';
    
    return accountNames[accountNo] || 'COMPTE TPE';
  }

  // Vérification TPE via backend ou simulation
  async verifyTPE(nAffiliation: string): Promise<TPEInfo> {
    if (this.useBackend) {
      try {
        return await firstValueFrom(this.tpePostingService.verifyTPE(nAffiliation));
      } catch (error) {
        console.error('Erreur lors de la vérification TPE:', error);
        // Fallback en simulation
        return this.verifyTPESimulation(nAffiliation);
      }
    } else {
      return this.verifyTPESimulation(nAffiliation);
    }
  }

  // Simulation locale TPE (mode déconnecté)
  verifyTPESimulation(nAffiliation: string): TPEInfo {
    const tpeRejetes: string[] = [];
    
    if (tpeRejetes.includes(nAffiliation)) {
      return {
        nAffiliation: nAffiliation,
        nCompte: '',
        exists: false,
        branch: '',
        profitCentre: '',
        clientId: ''
      };
    }
    
    return {
      nAffiliation: nAffiliation,
      nCompte: '999-TR-910234-150.1103.0000',
      exists: true,
      branch: '999',
      profitCentre: 'TR',
      clientId: '910234'
    };
  }

  // Vérification PORTEUR via backend ou simulation
  async verifyPorteur(ncarte: string): Promise<PorteurInfo> {
    if (this.useBackend) {
      try {
        return await firstValueFrom(this.tpePostingService.verifyPorteur(ncarte));
      } catch (error) {
        console.error('Erreur lors de la vérification Porteur:', error);
        // Fallback en simulation
        return this.verifyPorteurSimulation(ncarte);
      }
    } else {
      return this.verifyPorteurSimulation(ncarte);
    }
  }

  // Simulation locale PORTEUR (mode déconnecté)
  verifyPorteurSimulation(ncarte: string): PorteurInfo {
    const cartesSpeciales: {[key: string]: PorteurInfo} = {
      '5000000000000001': {
        ncarte: '5000000000000001',
        compte: 'IK-IK-918671-150.1206.0000',
        devise: 'EUR',
        ccyId: 'EUR',
        ccyRate: 3.35,
        deciPlaces: 2,
        exists: true,
        branch: 'IK',
        profitCentre: 'IK',
        clientId: '918671'
      },
      '5000000000000002': {
        ncarte: '5000000000000002',
        compte: 'IK-IK-918671-150.1206.0000',
        devise: 'USD',
        ccyId: 'USD',
        ccyRate: 3.10,
        deciPlaces: 2,
        exists: true,
        branch: 'IK',
        profitCentre: 'IK',
        clientId: '918671'
      }
    };
    
    if (cartesSpeciales[ncarte]) {
      return cartesSpeciales[ncarte];
    }
    
    return {
      ncarte: ncarte,
      compte: 'IK-IK-918671-150.1206.0000',
      devise: 'TND',
      ccyId: 'TND',
      ccyRate: 1.0,
      deciPlaces: 3,
      exists: true,
      branch: 'IK',
      profitCentre: 'IK',
      clientId: '918671'
    };
  }

  // Sauvegarder les écritures dans la base de données
  async saveToBackend(): Promise<void> {
    if (!this.processingResult) return;
    
    const allEcritures: EcritureComptable[] = [];
    this.processingResult.transactions.forEach(t => {
      t.ecrituresComptables.forEach(e => {
        allEcritures.push(e);
      });
    });
    
    try {
      const result = await firstValueFrom(this.tpePostingService.insertPostings(allEcritures));
      console.log(`${result.insertedCount} écritures insérées dans la base`);
      this.message += ` | ${result.insertedCount} écritures sauvegardées dans la base`;
    } catch (error: any) {
      console.error('Erreur lors de l\'insertion en base:', error);
      this.message += ' | Erreur lors de la sauvegarde en base';
    }
  }

  clearSelection(): void {
    this.selectedFile = null;
    this.uploadSuccess = false;
    this.uploadError = false;
    this.message = '';
    this.processingResult = null;
    this.backendProcessed = false;
    this.lastSessionDate = '';
  }

  generatePDFReport(): void {
    if (!this.processingResult) return;

    const doc = new jsPDF('landscape'); // Mode paysage pour plus de colonnes
    const currentDate = new Date().toLocaleDateString('fr-FR');

    // En-tête du rapport
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.text('Rapport d\'Analyse du Fichier Bancaire', 148, 15, { align: 'center' });
    
    doc.setFontSize(11);
    doc.setFont('helvetica', 'normal');
    doc.text(`Bank ABC Tunisie - Direction Monétique`, 148, 22, { align: 'center' });
    doc.text(`Date: ${currentDate}`, 148, 28, { align: 'center' });
    
    doc.setFontSize(9);
    doc.text(`Fichier traité: ${this.selectedFile?.name || 'N/A'}`, 14, 35);

    // Information d'en-tête du fichier
    if (this.processingResult.header) {
      doc.text(`Code Banque: ${this.processingResult.header.bankCode} | Date fichier: ${this.processingResult.header.date}`, 14, 40);
    }

    // Ligne séparatrice
    doc.setDrawColor(0, 51, 102);
    doc.setLineWidth(0.5);
    doc.line(14, 43, 282, 43);

    // Résumé du traitement
    doc.setFontSize(14);
    doc.setFont('helvetica', 'bold');
    doc.text('Résumé du Traitement', 14, 50);
    
    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    
    const summaryData = [
      ['Total de lignes', this.processingResult.totalLines.toString()],
      ['Lignes traitées avec succès', this.processingResult.processedLines.toString()],
      ['Lignes en erreur', this.processingResult.errorLines.toString()],
      ['Taux de réussite', `${((this.processingResult.processedLines / this.processingResult.totalLines) * 100).toFixed(2)}%`]
    ];

    autoTable(doc, {
      startY: 53,
      head: [['Critère', 'Valeur']],
      body: summaryData,
      theme: 'grid',
      headStyles: { fillColor: [0, 51, 102], textColor: 255 },
      margin: { left: 14 },
      columnStyles: {
        0: { cellWidth: 150 },
        1: { cellWidth: 50, halign: 'center' }
      }
    });

    // Écritures comptables
    let finalY = (doc as any).lastAutoTable.finalY + 10;
    
    if (this.processingResult.transactions && this.processingResult.transactions.length > 0) {
      doc.setFontSize(14);
      doc.setFont('helvetica', 'bold');
      doc.text('Écritures Comptables (TPE_POSTING_comp)', 14, finalY);
      
      // Collecter toutes les écritures
      const allEcritures: any[] = [];
      this.processingResult.transactions.forEach(t => {
        t.ecrituresComptables.forEach(e => {
          allEcritures.push([
            e.branch,
            e.profitCentre,
            e.clientId,
            e.accountNo,
            e.accountName.substring(0, 25),
            e.accountType,
            e.ccy || 'TND',
            e.seqNo,
            e.referenceNo,
            e.rbTranType || '',
            e.valueDate,
            e.amount,
            e.dc
          ]);
        });
      });
      
      autoTable(doc, {
        startY: finalY + 4,
        head: [['Branch', 'Profit Centre', 'Client Id', 'Account No', 'Account Name', 'Account Type', 'Ccy', 'Seq No', 'Reference No', 'Rb Tran Type', 'Value Date', 'Amount', 'Dr Cr']],
        body: allEcritures,
        theme: 'striped',
        headStyles: { fillColor: [0, 123, 255], textColor: 255, fontSize: 7, fontStyle: 'bold' },
        bodyStyles: { fontSize: 6 },
        margin: { left: 10, right: 10 },
        columnStyles: {
          0: { cellWidth: 18, halign: 'center' },      // Branch
          1: { cellWidth: 22, halign: 'center' },      // Profit Centre
          2: { cellWidth: 20 },                        // Client Id
          3: { cellWidth: 28 },                        // Account No
          4: { cellWidth: 48 },                        // Account Name
          5: { cellWidth: 22, halign: 'center' },      // Account Type
          6: { cellWidth: 12, halign: 'center' },      // Ccy
          7: { cellWidth: 15, halign: 'center' },      // Seq No
          8: { cellWidth: 25 },                        // Reference No
          9: { cellWidth: 20, halign: 'center' },      // Rb Tran Type
          10: { cellWidth: 22, halign: 'center' },     // Value Date
          11: { cellWidth: 22, halign: 'right' },      // Amount
          12: { cellWidth: 15, halign: 'center' }      // Dr Cr
        }
      });

      finalY = (doc as any).lastAutoTable.finalY + 10;
    }

    // Erreurs rencontrées
    if (this.processingResult.errors && this.processingResult.errors.length > 0) {
      if (finalY > 180) {
        doc.addPage();
        finalY = 20;
      }

      doc.setFontSize(14);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(220, 53, 69);
      doc.text('Erreurs Rencontrées', 14, finalY);
      
      doc.setTextColor(0, 0, 0);
      
      const errorData = this.processingResult.errors.map(e => [
        e.lineNumber.toString(),
        e.content.substring(0, 100) + '...',
        e.error
      ]);

      autoTable(doc, {
        startY: finalY + 4,
        head: [['Ligne', 'Contenu', 'Erreur']],
        body: errorData,
        theme: 'grid',
        headStyles: { fillColor: [220, 53, 69], textColor: 255, fontSize: 8 },
        bodyStyles: { fontSize: 7 },
        margin: { left: 14, right: 14 },
        columnStyles: {
          0: { cellWidth: 20 },
          1: { cellWidth: 150 },
          2: { cellWidth: 80 }
        }
      });
    }

    // Pied de page
    const pageCount = doc.internal.pages.length - 1;
    for (let i = 1; i <= pageCount; i++) {
      doc.setPage(i);
      doc.setFontSize(8);
      doc.setFont('helvetica', 'italic');
      doc.setTextColor(128, 128, 128);
      doc.text(`Page ${i} / ${pageCount}`, 148, 200, { align: 'center' });
      doc.text('Document confidentiel - Bank ABC Tunisie', 148, 205, { align: 'center' });
    }

    // Téléchargement du PDF
    const fileName = `Rapport_Fichier_Bancaire_${new Date().getTime()}.pdf`;
    doc.save(fileName);
  }

  generateTXTReport(): void {
    if (!this.processingResult) return;

    const currentDate = new Date().toLocaleString('fr-FR');
    let report = '';

    // En-tête
    report += '═'.repeat(180) + '\n';
    report += this.centerText('RAPPORT D\'ANALYSE DU FICHIER BANCAIRE', 180) + '\n';
    report += this.centerText('Bank ABC Tunisie - Direction Monétique', 180) + '\n';
    report += this.centerText(`Date: ${currentDate}`, 180) + '\n';
    report += '═'.repeat(180) + '\n\n';

    report += `Fichier traité: ${this.selectedFile?.name || 'N/A'}\n`;
    
    // Information d'en-tête du fichier
    if (this.processingResult.header) {
      report += `Code Banque: ${this.processingResult.header.bankCode}\n`;
      report += `Date du fichier: ${this.processingResult.header.date}\n`;
      report += `Numéro de séquence: ${this.processingResult.header.sequenceNumber}\n`;
    }
    report += '\n';

    // Résumé
    report += '─'.repeat(180) + '\n';
    report += 'RÉSUMÉ DU TRAITEMENT\n';
    report += '─'.repeat(180) + '\n';
    report += this.formatLine('Total de lignes', this.processingResult.totalLines.toString(), 80) + '\n';
    report += this.formatLine('Lignes traitées avec succès', this.processingResult.processedLines.toString(), 80) + '\n';
    report += this.formatLine('Lignes en erreur', this.processingResult.errorLines.toString(), 80) + '\n';
    report += this.formatLine('Taux de réussite', 
      `${((this.processingResult.processedLines / this.processingResult.totalLines) * 100).toFixed(2)}%`, 80) + '\n';
    report += '─'.repeat(180) + '\n\n';

    // Écritures comptables
    if (this.processingResult.transactions && this.processingResult.transactions.length > 0) {
      report += '─'.repeat(180) + '\n';
      report += 'ÉCRITURES COMPTABLES (TPE_POSTING_comp)\n';
      report += '─'.repeat(180) + '\n\n';
      
      report += this.padRight('Branch', 8) + ' | ';
      report += this.padRight('Profit Centre', 14) + ' | ';
      report += this.padRight('Client Id', 12) + ' | ';
      report += this.padRight('Account No', 20) + ' | ';
      report += this.padRight('Account Name', 35) + ' | ';
      report += this.padRight('Account Type', 13) + ' | ';
      report += this.padRight('Ccy', 5) + ' | ';
      report += this.padRight('Seq No', 8) + ' | ';
      report += this.padRight('Reference No', 16) + ' | ';
      report += this.padRight('Rb Tran Type', 13) + ' | ';
      report += this.padRight('Value Date', 11) + ' | ';
      report += this.padLeft('Amount', 12) + ' | ';
      report += this.padRight('Dr Cr', 5) + '\n';
      report += '─'.repeat(180) + '\n';
      
      this.processingResult.transactions.forEach(t => {
        t.ecrituresComptables.forEach(e => {
          report += this.padRight(e.branch, 8) + ' | ';
          report += this.padRight(e.profitCentre, 14) + ' | ';
          report += this.padRight(e.clientId, 12) + ' | ';
          report += this.padRight(e.accountNo, 20) + ' | ';
          report += this.padRight(e.accountName.substring(0, 35), 35) + ' | ';
          report += this.padRight(e.accountType, 13) + ' | ';
          report += this.padRight(e.ccy || 'TND', 5) + ' | ';
          report += this.padRight(e.seqNo, 8) + ' | ';
          report += this.padRight(e.referenceNo, 16) + ' | ';
          report += this.padRight(e.rbTranType || '', 13) + ' | ';
          report += this.padRight(e.valueDate, 11) + ' | ';
          report += this.padLeft(e.amount, 12) + ' | ';
          report += this.padRight(e.dc, 5) + '\n';
        });
      });
      
      report += '─'.repeat(180) + '\n\n';
      
      // Statistiques
      const totalType10 = this.processingResult.transactions.filter(t => t.recordType === '10').length;
      const totalType20 = this.processingResult.transactions.filter(t => t.recordType === '20').length;
      const totalEcritures = this.processingResult.transactions.reduce((sum, t) => sum + t.ecrituresComptables.length, 0);
      
      report += 'STATISTIQUES:\n';
      report += this.formatLine('Transactions Type 10 (TPE)', totalType10.toString(), 80) + '\n';
      report += this.formatLine('Transactions Type 20 (Porteur)', totalType20.toString(), 80) + '\n';
      report += this.formatLine('Total écritures comptables générées', totalEcritures.toString(), 80) + '\n';
      report += '─'.repeat(180) + '\n\n';
    }

    // Erreurs
    if (this.processingResult.errors && this.processingResult.errors.length > 0) {
      report += '─'.repeat(180) + '\n';
      report += 'ERREURS RENCONTRÉES\n';
      report += '─'.repeat(180) + '\n\n';
      
      this.processingResult.errors.forEach((e, index) => {
        report += `Erreur ${index + 1}:\n`;
        report += `  Ligne: ${e.lineNumber}\n`;
        report += `  Contenu: ${e.content.substring(0, 160)}...\n`;
        report += `  Erreur: ${e.error}\n\n`;
      });
    }

    // Pied de page
    report += '═'.repeat(180) + '\n';
    report += this.centerText('Document confidentiel - Bank ABC Tunisie', 180) + '\n';
    report += '═'.repeat(180) + '\n';

    // Téléchargement du fichier texte
    const blob = new Blob([report], { type: 'text/plain;charset=utf-8' });
    const fileName = `Rapport_Fichier_Bancaire_${new Date().getTime()}.txt`;
    saveAs(blob, fileName);
  }

  // Fonctions utilitaires pour le formatage
  private centerText(text: string, width: number): string {
    const padding = Math.max(0, Math.floor((width - text.length) / 2));
    return ' '.repeat(padding) + text;
  }

  private formatLine(label: string, value: string, width: number): string {
    const dots = '.'.repeat(Math.max(0, width - label.length - value.length));
    return `${label} ${dots} ${value}`;
  }

  private padRight(text: string, width: number): string {
    return text.substring(0, width).padEnd(width, ' ');
  }

  private padLeft(text: string, width: number): string {
    return text.substring(0, width).padStart(width, ' ');
  }
}
