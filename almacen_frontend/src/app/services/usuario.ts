import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { environment } from '../../environments/environment';
import { Usuario, UsuarioRequest, UsuarioUpdateRequest } from '../models/usuario';

/** Administración de usuarios (solo ADMIN). */
@Service()
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/usuarios`;

  listar() {
    return this.http.get<Usuario[]>(this.url);
  }

  crear(usuario: UsuarioRequest) {
    return this.http.post<Usuario>(this.url, usuario);
  }

  /** Cambia nombre, rol y estado (activo / inactivo). */
  actualizar(id: number, datos: UsuarioUpdateRequest) {
    return this.http.put<Usuario>(`${this.url}/${id}`, datos);
  }

  restablecerPassword(id: number, nueva: string) {
    return this.http.put<void>(`${this.url}/${id}/password`, { nueva });
  }
}
