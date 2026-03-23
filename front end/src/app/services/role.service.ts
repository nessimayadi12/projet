import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface RoleDTO {
  id: number;
  name: string;
  label?: string;
}

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  private apiUrl = `${environment.apiUrl}/roles`;

  constructor(private http: HttpClient) { }

  getAllRoles(): Observable<RoleDTO[]> {
    return this.http.get<RoleDTO[]>(this.apiUrl);
  }
}
