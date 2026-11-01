import { Type } from '@angular/core';
import { Rol } from '../models/usuario';
import { CompraNueva } from '../pages/compra-nueva/compra-nueva';
import { Costos } from '../pages/costos/costos';
import { MovimientosLista } from '../pages/movimientos/movimientos';
import { Precios } from '../pages/precios/precios';
import { Productos } from '../pages/productos/productos';
import { TrasladoNuevo } from '../pages/traslado-nuevo/traslado-nuevo';
import { Usuarios } from '../pages/usuarios/usuarios';
import { VentaNueva } from '../pages/venta-nueva/venta-nueva';

export interface Opcion {
  /** Ruta dentro del módulo: la URL final es /<modulo>/<ruta>. */
  ruta: string;
  titulo: string;
  detalle: string;
  /** Roles que la usan (ADMIN entra a todo). */
  roles: Rol[];
  componente: Type<unknown>;
  datos?: Record<string, unknown>;
}

export interface Modulo {
  id: string;
  titulo: string;
  detalle: string;
  opciones: Opcion[];
}

/** Módulos del sistema y sus opciones. Las rutas, los permisos y los menús salen de aquí. */
export const MODULOS: Modulo[] = [
  {
    id: 'comercial',
    titulo: 'Comercial',
    detalle: 'Precios de venta y ventas',
    opciones: [
      {
        ruta: 'precios',
        titulo: 'Precios de venta',
        detalle: 'Consultar y actualizar precios',
        roles: ['COMERCIAL'],
        componente: Precios,
      },
      {
        ruta: 'ventas/nueva',
        titulo: 'Registrar venta',
        detalle: 'Factura, boleta o ticket',
        roles: ['COMERCIAL'],
        componente: VentaNueva,
      },
      {
        ruta: 'ventas',
        titulo: 'Ver ventas',
        detalle: 'Historial de egresos por venta',
        roles: ['COMERCIAL'],
        componente: MovimientosLista,
        datos: { tipo: 'VENTA', titulo: 'Ventas' },
      },
    ],
  },
  {
    id: 'logistica',
    titulo: 'Logística',
    detalle: 'Compras, productos, stock y traslados',
    opciones: [
      {
        ruta: 'compras/nueva',
        titulo: 'Registrar compra',
        detalle: 'Ingreso por orden de compra',
        roles: ['COMPRAS'],
        componente: CompraNueva,
      },
      {
        ruta: 'compras',
        titulo: 'Ver compras',
        detalle: 'Historial de ingresos por compra',
        roles: ['COMPRAS'],
        componente: MovimientosLista,
        datos: { tipo: 'COMPRA', titulo: 'Compras' },
      },
      {
        ruta: 'costos',
        titulo: 'Costos',
        detalle: 'Costo promedio (solo lectura)',
        roles: ['COMPRAS'],
        componente: Costos,
      },
      {
        ruta: 'productos',
        titulo: 'Productos',
        detalle: 'Registrar productos y ver stock',
        roles: ['COMPRAS', 'ALMACEN'],
        componente: Productos,
      },
      {
        ruta: 'traslados/nuevo',
        titulo: 'Registrar traslado',
        detalle: 'Entre almacenes',
        roles: ['ALMACEN'],
        componente: TrasladoNuevo,
      },
      {
        ruta: 'traslados',
        titulo: 'Ver traslados',
        detalle: 'Historial de traslados internos',
        roles: ['ALMACEN'],
        componente: MovimientosLista,
        datos: { tipo: 'TRASLADO', titulo: 'Traslados internos' },
      },
      {
        ruta: 'movimientos',
        titulo: 'Movimientos',
        detalle: 'Compras, ventas y traslados',
        roles: ['ALMACEN'],
        componente: MovimientosLista,
        datos: { tipo: null, titulo: 'Movimientos de inventario' },
      },
    ],
  },
  {
    id: 'administracion',
    titulo: 'Administración',
    detalle: 'Usuarios y roles',
    opciones: [
      {
        ruta: 'usuarios',
        titulo: 'Usuarios',
        detalle: 'Altas, roles y contraseñas',
        roles: [],
        componente: Usuarios,
      },
    ],
  },
];

/** Roles que tienen al menos una opción en el módulo. */
export function rolesDelModulo(modulo: Modulo): Rol[] {
  return [...new Set(modulo.opciones.flatMap((o) => o.roles))];
}
