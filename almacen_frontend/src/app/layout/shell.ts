import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth';

interface Enlace {
  ruta: string;
  texto: string;
  exacto?: boolean;
}

interface Grupo {
  titulo: string;
  enlaces: Enlace[];
}

/** Estructura común de las pantallas internas: barra superior + menú lateral. */
@Component({
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  selector: 'app-shell',
  templateUrl: './shell.html',
  styleUrl: './shell.css',
})
export class Shell {
  protected readonly auth = inject(AuthService);

  /** El grupo "Administración" solo aparece para el rol ADMIN. */
  protected readonly menu = computed<Grupo[]>(() =>
    this.auth.esAdmin()
      ? [...this.menuBase, { titulo: 'Administración', enlaces: [{ ruta: '/usuarios', texto: 'Usuarios' }] }]
      : this.menuBase,
  );

  private readonly menuBase: Grupo[] = [
    { titulo: 'General', enlaces: [{ ruta: '/', texto: 'Menú principal', exacto: true }] },
    {
      titulo: 'Inventario',
      enlaces: [
        { ruta: '/productos', texto: 'Productos' },
        { ruta: '/precios', texto: 'Precios de venta' },
        { ruta: '/costos', texto: 'Costos' },
      ],
    },
    {
      titulo: 'Compras (ingresos)',
      enlaces: [
        { ruta: '/compras/nueva', texto: 'Registrar compra' },
        { ruta: '/compras', texto: 'Ver compras', exacto: true },
      ],
    },
    {
      titulo: 'Ventas (egresos)',
      enlaces: [
        { ruta: '/ventas/nueva', texto: 'Registrar venta' },
        { ruta: '/ventas', texto: 'Ver ventas', exacto: true },
      ],
    },
    {
      titulo: 'Traslados internos',
      enlaces: [
        { ruta: '/traslados/nuevo', texto: 'Registrar traslado' },
        { ruta: '/traslados', texto: 'Ver traslados', exacto: true },
      ],
    },
    { titulo: 'Consultas', enlaces: [{ ruta: '/movimientos', texto: 'Movimientos' }] },
  ];
}
