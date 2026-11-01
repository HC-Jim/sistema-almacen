import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { mensajeError } from '../../core/errores';
import { Almacen } from '../../models/almacen';
import { Movimiento, TipoMovimiento } from '../../models/movimiento';
import { AlmacenService } from '../../services/almacen';
import { MovimientoService } from '../../services/movimiento';

/**
 * Listado de movimientos. La misma pantalla sirve para "Ver compras", "Ver ventas",
 * "Ver traslados" y "Movimientos" (todos); el tipo llega desde la ruta.
 */
@Component({
  imports: [FormsModule, DatePipe, DecimalPipe, RouterLink],
  selector: 'app-movimientos',
  templateUrl: './movimientos.html',
})
export class MovimientosLista implements OnInit {
  private readonly movimientoService = inject(MovimientoService);
  private readonly almacenService = inject(AlmacenService);

  /** Tipo fijo de la pantalla (null = todos). Viene del "data" de la ruta. */
  readonly tipo = input<TipoMovimiento | null>(null);
  readonly titulo = input('Movimientos');

  protected readonly movimientos = signal<Movimiento[]>([]);
  protected readonly almacenes = signal<Almacen[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly abiertos = signal<Set<number>>(new Set());

  protected filtroTipo: TipoMovimiento | '' = '';
  protected filtroAlmacen: number | '' = '';
  protected desde = '';
  protected hasta = '';

  protected readonly total = computed(() => this.movimientos().reduce((suma, m) => suma + m.total, 0));

  ngOnInit(): void {
    this.almacenService.listar().subscribe((data) => this.almacenes.set(data));
    this.buscar();
  }

  protected rutaNueva(): string | null {
    switch (this.tipo()) {
      case 'COMPRA':
        return '/logistica/compras/nueva';
      case 'VENTA':
        return '/comercial/ventas/nueva';
      case 'TRASLADO':
        return '/logistica/traslados/nuevo';
      default:
        return null;
    }
  }

  protected buscar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.movimientoService
      .listar({
        tipo: this.tipo() ?? (this.filtroTipo || null),
        almacenId: this.filtroAlmacen || null,
        desde: this.desde || null,
        hasta: this.hasta || null,
      })
      .subscribe({
        next: (data) => {
          this.movimientos.set(data);
          this.cargando.set(false);
        },
        error: (err) => {
          this.cargando.set(false);
          this.error.set(mensajeError(err));
        },
      });
  }

  protected limpiarFiltros(): void {
    this.filtroTipo = '';
    this.filtroAlmacen = '';
    this.desde = '';
    this.hasta = '';
    this.buscar();
  }

  protected alternar(id: number): void {
    this.abiertos.update((set) => {
      const nuevo = new Set(set);
      if (nuevo.has(id)) {
        nuevo.delete(id);
      } else {
        nuevo.add(id);
      }
      return nuevo;
    });
  }

  protected documento(m: Movimiento): string {
    if (m.tipo === 'COMPRA') {
      return `OC ${m.ordenCompra} · ${m.tipoCompra}`;
    }
    if (m.tipo === 'VENTA') {
      return `${m.tipoComprobante} ${m.numeroComprobante}`;
    }
    return '—';
  }

  protected almacen(m: Movimiento): string {
    if (m.tipo === 'COMPRA') {
      return `→ ${m.almacenDestino?.codigo}`;
    }
    if (m.tipo === 'VENTA') {
      return `${m.almacenOrigen?.codigo} →`;
    }
    return `${m.almacenOrigen?.codigo} → ${m.almacenDestino?.codigo}`;
  }

  protected tercero(m: Movimiento): string {
    if (m.tipo === 'COMPRA') {
      return m.proveedor ?? '—';
    }
    if (m.tipo === 'VENTA') {
      return `DNI ${m.clienteDni}${m.clienteNombre ? ' · ' + m.clienteNombre : ''} · ${m.medioPago}`;
    }
    return m.observacion ?? '—';
  }

  protected columnaTercero(): string {
    switch (this.tipo()) {
      case 'COMPRA':
        return 'Proveedor';
      case 'VENTA':
        return 'Cliente / pago';
      case 'TRASLADO':
        return 'Motivo';
      default:
        return 'Proveedor / cliente';
    }
  }
}
