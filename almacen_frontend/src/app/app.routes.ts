import { Routes } from '@angular/router';
import { adminGuard, authGuard, invitadoGuard } from './core/auth.guard';
import { Shell } from './layout/shell';
import { CambiarPassword } from './pages/cambiar-password/cambiar-password';
import { CompraNueva } from './pages/compra-nueva/compra-nueva';
import { Costos } from './pages/costos/costos';
import { Inicio } from './pages/inicio/inicio';
import { Login } from './pages/login/login';
import { MovimientosLista } from './pages/movimientos/movimientos';
import { Precios } from './pages/precios/precios';
import { Productos } from './pages/productos/productos';
import { TrasladoNuevo } from './pages/traslado-nuevo/traslado-nuevo';
import { Usuarios } from './pages/usuarios/usuarios';
import { VentaNueva } from './pages/venta-nueva/venta-nueva';

export const routes: Routes = [
  { path: 'login', component: Login, canActivate: [invitadoGuard], title: 'Ingresar' },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: '', component: Inicio, title: 'Menú principal' },
      { path: 'productos', component: Productos, title: 'Productos' },
      { path: 'precios', component: Precios, title: 'Precios de venta' },
      { path: 'costos', component: Costos, title: 'Costos' },
      { path: 'compras/nueva', component: CompraNueva, title: 'Registrar compra' },
      { path: 'compras', component: MovimientosLista, title: 'Compras', data: { tipo: 'COMPRA', titulo: 'Compras' } },
      { path: 'ventas/nueva', component: VentaNueva, title: 'Registrar venta' },
      { path: 'ventas', component: MovimientosLista, title: 'Ventas', data: { tipo: 'VENTA', titulo: 'Ventas' } },
      { path: 'traslados/nuevo', component: TrasladoNuevo, title: 'Registrar traslado' },
      {
        path: 'traslados',
        component: MovimientosLista,
        title: 'Traslados',
        data: { tipo: 'TRASLADO', titulo: 'Traslados internos' },
      },
      {
        path: 'movimientos',
        component: MovimientosLista,
        title: 'Movimientos',
        data: { tipo: null, titulo: 'Movimientos de inventario' },
      },
      { path: 'cambiar-password', component: CambiarPassword, title: 'Cambiar contraseña' },
      { path: 'usuarios', component: Usuarios, canActivate: [adminGuard], title: 'Usuarios' },
    ],
  },
  { path: '**', redirectTo: '' },
];
