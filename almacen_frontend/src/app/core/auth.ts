import { HttpClient } from '@angular/common/http';
import { computed, inject, Service, signal } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Sesion {
  token: string;
  expiraEn: string;
  username: string;
  nombre: string;
  rol: string;
}

const CLAVE = 'almacen.sesion';

/** Maneja el login: guarda el token JWT y sabe si la sesión sigue vigente. */
@Service()
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly sesion = signal<Sesion | null>(leer());
  readonly autenticado = computed(() => {
    const s = this.sesion();
    return s !== null && new Date(s.expiraEn).getTime() > Date.now();
  });
  readonly esAdmin = computed(() => this.autenticado() && this.sesion()!.rol === 'ADMIN');

  login(username: string, password: string) {
    return this.http
      .post<Sesion>(`${environment.apiUrl}/auth/login`, { username, password })
      .pipe(tap((sesion) => this.guardar(sesion)));
  }

  /** Cambia la contraseña del usuario conectado (el backend exige la actual). */
  cambiarPassword(actual: string, nueva: string) {
    return this.http.put<void>(`${environment.apiUrl}/auth/password`, { actual, nueva });
  }

  logout(): void {
    try {
      localStorage.removeItem(CLAVE);
    } catch {
      /* almacenamiento no disponible */
    }
    this.sesion.set(null);
    this.router.navigate(['/login']);
  }

  token(): string | null {
    return this.autenticado() ? this.sesion()!.token : null;
  }

  private guardar(sesion: Sesion): void {
    try {
      localStorage.setItem(CLAVE, JSON.stringify(sesion));
    } catch {
      /* si no se puede guardar, la sesión dura mientras la pestaña esté abierta */
    }
    this.sesion.set(sesion);
  }
}

function leer(): Sesion | null {
  try {
    const texto = localStorage.getItem(CLAVE);
    return texto ? (JSON.parse(texto) as Sesion) : null;
  } catch {
    return null;
  }
}
