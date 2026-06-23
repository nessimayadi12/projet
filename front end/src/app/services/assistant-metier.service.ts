import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  AssistantRequestDTO,
  AssistantResponseDTO
} from '../models/assistant-metier.model';

@Injectable({
  providedIn: 'root'
})
export class AssistantMetierService {
  private apiUrl = `${environment.apiUrl}/assistant`;

  constructor(private http: HttpClient) { }

  interroger(request: AssistantRequestDTO): Observable<AssistantResponseDTO> {
    return this.http.post<AssistantResponseDTO>(`${this.apiUrl}/interroger`, request);
  }
}
