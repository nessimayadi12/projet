import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TauxTpeService } from './taux-tpe.service';
import { TauxTPE, StatutTaux } from '../models/taux-tpe.model';
import { environment } from '../../environments/environment';

describe('TauxTpeService - Processus 4 Yeux', () => {
  
  let service: TauxTpeService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/taux`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TauxTpeService]
    });

    service = TestBed.inject(TauxTpeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();  // Vérifier qu'aucune requête reste en attente
  });

  describe('✅ Création et Soumission', () => {
    
    it('should create new taux (INPUTER)', () => {
      const mockRequest = {
        commercantId: 1,
        nouveauTauxCommission: 1.5,
        nouveauTauxCommissionInter: 0.8,
        commentaire: 'Test'
      };

      const mockResponse: TauxTPE = {
        id: 123,
        commercantId: 1,
        commercantNom: 'Café Central',
        nouveauTauxCommission: 1.5,
        nouveauTauxCommissionInter: 0.8,
        statut: StatutTaux.BROUILLON,
        inputerId: 100,
        inputerNom: 'alice',
        dateSaisie: '2024-01-15T10:00:00',
        actif: false
      };

      service.createTaux(mockRequest).subscribe(response => {
        expect(response.id).toBe(123);
        expect(response.statut).toBe(StatutTaux.BROUILLON);
        expect(response.inputerNom).toBe('alice');
      });

      const req = httpMock.expectOne(`${apiUrl}`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockRequest);
      req.flush(mockResponse);
    });

    it('should submit taux for validation (INPUTER)', () => {
      const tauxId = 123;
      const mockResponse: TauxTPE = {
        id: tauxId,
        commercantId: 1,
        commercantNom: 'Café Central',
        nouveauTauxCommission: 1.5,
        nouveauTauxCommissionInter: 0.8,
        statut: StatutTaux.EN_ATTENTE_VALIDATION,
        inputerId: 100,
        inputerNom: 'alice',
        dateSaisie: '2024-01-15T10:00:00',
        actif: false
      };

      service.submitForValidation(tauxId).subscribe(response => {
        expect(response.statut).toBe(StatutTaux.EN_ATTENTE_VALIDATION);
      });

      const req = httpMock.expectOne(`${apiUrl}/${tauxId}/soumettre`);
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });
  });

  describe('✅ Validation (AUTHORIZER)', () => {
    
    it('should approve taux (AUTHORIZER != INPUTER)', () => {
      const tauxId = 123;
      const mockResponse: TauxTPE = {
        id: tauxId,
        commercantId: 1,
        commercantNom: 'Café Central',
        nouveauTauxCommission: 1.5,
        nouveauTauxCommissionInter: 0.8,
        statut: StatutTaux.VALIDE,
        inputerId: 100,
        inputerNom: 'alice',
        authorizerId: 200,
        authorizerNom: 'bob',
        dateSaisie: '2024-01-15T10:00:00',
        dateValidation: '2024-01-15T11:00:00',
        dateApplication: '2024-01-15T11:00:00',
        actif: true
      };

      service.approveTaux(tauxId).subscribe(response => {
        expect(response.statut).toBe(StatutTaux.VALIDE);
        expect(response.authorizerNom).toBe('bob');
        expect(response.actif).toBe(true);
      });

      const req = httpMock.expectOne(`${apiUrl}/${tauxId}/valider`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({
        approuver: true,
        motifRejet: null
      });
      req.flush(mockResponse);
    });

    it('should reject taux with motif', () => {
      const tauxId = 123;
      const motif = 'Taux trop élevé';

      const mockResponse: TauxTPE = {
        id: tauxId,
        commercantId: 1,
        commercantNom: 'Café Central',
        nouveauTauxCommission: 1.5,
        nouveauTauxCommissionInter: 0.8,
        statut: StatutTaux.REJETE,
        inputerId: 100,
        inputerNom: 'alice',
        authorizerId: 200,
        authorizerNom: 'bob',
        dateSaisie: '2024-01-15T10:00:00',
        dateValidation: '2024-01-15T11:00:00',
        motifRejet: motif,
        actif: false
      };

      service.rejectTaux(tauxId, motif).subscribe(response => {
        expect(response.statut).toBe(StatutTaux.REJETE);
        expect(response.motifRejet).toBe(motif);
      });

      const req = httpMock.expectOne(`${apiUrl}/${tauxId}/valider`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({
        approuver: false,
        motifRejet: motif
      });
      req.flush(mockResponse);
    });

    it('should handle 4-eyes error (INPUTER = AUTHORIZER)', () => {
      const tauxId = 123;
      const errorResponse = {
        message: 'Vous ne pouvez pas valider vos propres saisies (Règle 4 yeux)',
        status: 400
      };

      service.approveTaux(tauxId).subscribe(
        () => fail('Should have failed'),
        (error) => {
          expect(error.message).toContain('4 yeux');
        }
      );

      const req = httpMock.expectOne(`${apiUrl}/${tauxId}/valider`);
      req.flush(errorResponse, { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('✅ Récupération', () => {
    
    it('should get pending taux list (AUTHORIZER)', () => {
      const mockList: TauxTPE[] = [
        {
          id: 123,
          commercantId: 1,
          commercantNom: 'Café Central',
          nouveauTauxCommission: 1.5,
          nouveauTauxCommissionInter: 0.8,
          statut: StatutTaux.EN_ATTENTE_VALIDATION,
          inputerId: 100,
          inputerNom: 'alice',
          dateSaisie: '2024-01-15T10:00:00',
          actif: false
        }
      ];

      service.getTauxEnAttenteValidation().subscribe(response => {
        expect(response.length).toBe(1);
        expect(response[0].statut).toBe(StatutTaux.EN_ATTENTE_VALIDATION);
      });

      const req = httpMock.expectOne(`${apiUrl}/en-attente`);
      expect(req.request.method).toBe('GET');
      req.flush(mockList);
    });

    it('should get taux by commercant', () => {
      const commercantId = 1;
      const mockList: TauxTPE[] = [];

      service.getTauxByCommercant(commercantId).subscribe(response => {
        expect(Array.isArray(response)).toBe(true);
      });

      const req = httpMock.expectOne(`${apiUrl}/commercant/${commercantId}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockList);
    });
  });
});
