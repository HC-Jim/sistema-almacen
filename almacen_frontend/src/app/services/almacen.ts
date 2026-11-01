import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { environment } from '../../environments/environment';
import { Almacen } from '../models/almacen';

@Service()
export class AlmacenService {
  private readonly http = inject(HttpClient);

  listar() {
    return this.http.get<Almacen[]>(`${environment.apiUrl}/almacenes`);
  }
}
