import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Rol } from '../models/usuario';
import { AuthService } from './auth';

/** Solo deja entrar si hay sesión; si no, manda al login. */
export const authGuard: CanActivateFn = () =>
  inject(AuthService).autenticado() ? true : inject(Router).createUrlTree(['/login']);

/** Exige uno de los roles de data.roles (ADMIN siempre pasa); si no, vuelve al menú principal. */
export const rolGuard: CanActivateFn = (route) =>
  inject(AuthService).puede((route.data['roles'] as Rol[]) ?? []) ? true : inject(Router).createUrlTree(['/']);

/** Si ya hay sesión, el login redirige al menú principal. */
export const invitadoGuard: CanActivateFn = () =>
  inject(AuthService).autenticado() ? inject(Router).createUrlTree(['/']) : true;
