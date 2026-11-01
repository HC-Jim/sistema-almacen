import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth';
import { Almacen } from '../../models/almacen';
import { Producto, stockEn } from '../../models/producto';
import { AlmacenService } from '../../services/almacen';
import { ProductoService } from '../../services/producto';

interface Opcion {
  ruta: string;
  titulo: string;
  detalle: string;
}

/** Menú principal: accesos a cada módulo y un resumen del inventario. */
@Component({
  imports: [RouterLink, DecimalPipe],
  selector: 'app-inicio',
  templateUrl: './inicio.html',
  styleUrl: './inicio.css',
})
export class Inicio implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly productoService = inject(ProductoService);
  private readonly almacenService = inject(AlmacenService);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly almacenes = signal<Almacen[]>([]);

  protected readonly valorizado = computed(() =>
    this.productos().reduce((suma, p) => suma + p.stock * p.costoPromedio, 0),
  );
  protected readonly stockTotal = computed(() => this.productos().reduce((suma, p) => suma + p.stock, 0));

  protected readonly opciones: Opcion[] = [
    { ruta: '/productos', titulo: 'Productos', detalle: 'Registrar y actualizar productos' },
    { ruta: '/precios', titulo: 'Precios de venta', detalle: 'Consultar y modificar precios' },
    { ruta: '/costos', titulo: 'Costos', detalle: 'Costo promedio (solo lectura)' },
    { ruta: '/compras/nueva', titulo: 'Registrar compra', detalle: 'Ingreso por orden de compra' },
    { ruta: '/ventas/nueva', titulo: 'Registrar venta', detalle: 'Factura, boleta o ticket' },
    { ruta: '/traslados/nuevo', titulo: 'Traslado interno', detalle: 'Entre almacenes' },
    { ruta: '/compras', titulo: 'Ver compras', detalle: 'Historial de ingresos' },
    { ruta: '/ventas', titulo: 'Ver ventas', detalle: 'Historial de egresos' },
    { ruta: '/movimientos', titulo: 'Movimientos', detalle: 'Todos los movimientos' },
  ];

  ngOnInit(): void {
    this.productoService.listar().subscribe((data) => this.productos.set(data));
    this.almacenService.listar().subscribe((data) => this.almacenes.set(data));
  }

  protected stockAlmacen(almacenId: number): number {
    return this.productos().reduce((suma, p) => suma + stockEn(p, almacenId), 0);
  }
}
