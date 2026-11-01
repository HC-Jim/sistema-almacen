import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth';

/** Solo deja entrar si hay sesión; si no, manda al login. */
export const authGuard: CanActivateFn = () =>
  inject(AuthService).autenticado() ? true : inject(Router).createUrlTree(['/login']);

/** Solo para administradores; los demás vuelven al menú principal. */
export const adminGuard: CanActivateFn = () =>
  inject(AuthService).esAdmin() ? true : inject(Router).createUrlTree(['/']);

/** Si ya hay sesión, el login redirige al menú principal. */
export const invitadoGuard: CanActivateFn = () =>
  inject(AuthService).autenticado() ? inject(Router).createUrlTree(['/']) : true;
