import { Routes } from '@angular/router';
import { authGuard, invitadoGuard, rolGuard } from './core/auth.guard';
import { MODULOS, rolesDelModulo } from './core/modulos';
import { Shell } from './layout/shell';
import { Inicio } from './pages/inicio/inicio';
import { Login } from './pages/login/login';
import { ModuloPagina } from './pages/modulo/modulo';

/** Cada módulo tiene su pantalla (/comercial) y sus opciones (/comercial/precios), protegidas por rol. */
const rutasDeModulos: Routes = MODULOS.flatMap((m) => [
  {
    path: m.id,
    component: ModuloPagina,
    canActivate: [rolGuard],
    title: m.titulo,
    data: { modulo: m.id, roles: rolesDelModulo(m) },
  },
  ...m.opciones.map((o) => ({
    path: `${m.id}/${o.ruta}`,
    component: o.componente,
    canActivate: [rolGuard],
    title: o.titulo,
    data: { ...o.datos, roles: o.roles },
  })),
]);

export const routes: Routes = [
  { path: 'login', component: Login, canActivate: [invitadoGuard], title: 'Ingresar' },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [{ path: '', component: Inicio, title: 'Menú principal' }, ...rutasDeModulos],
  },
  { path: '**', redirectTo: '' },
];
