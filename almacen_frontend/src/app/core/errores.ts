import { HttpErrorResponse } from '@angular/common/http';

/** Convierte un error HTTP en un mensaje legible para el usuario. */
export function mensajeError(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'No se pudo conectar con el backend (¿está corriendo en http://localhost:8080?).';
    }
    return err.error?.detail ?? `Error ${err.status} al llamar a la API.`;
  }
  return 'Ocurrió un error inesperado.';
}
