import { DecimalPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { mensajeError } from '../../core/errores';
import { Almacen } from '../../models/almacen';
import {
  MedioPago,
  MEDIOS_PAGO,
  Movimiento,
  TipoComprobante,
  TIPOS_COMPROBANTE,
  VentaRequest,
} from '../../models/movimiento';
import { Producto, stockEn } from '../../models/producto';
import { AlmacenService } from '../../services/almacen';
import { MovimientoService } from '../../services/movimiento';
import { ProductoService } from '../../services/producto';

/** Egreso por venta: comprobante, cliente (DNI), medio de pago y productos. */
@Component({
  imports: [ReactiveFormsModule, DecimalPipe, RouterLink],
  selector: 'app-venta-nueva',
  templateUrl: './venta-nueva.html',
})
export class VentaNueva implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly productoService = inject(ProductoService);
  private readonly almacenService = inject(AlmacenService);
  private readonly movimientoService = inject(MovimientoService);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly almacenes = signal<Almacen[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly registrada = signal<Movimiento | null>(null);
  protected readonly guardando = signal(false);

  protected readonly tiposComprobante = TIPOS_COMPROBANTE;
  protected readonly mediosPago = MEDIOS_PAGO;

  protected readonly form = this.fb.group({
    almacenId: this.fb.control<number | null>(null, Validators.required),
    tipoComprobante: this.fb.control<TipoComprobante>('BOLETA'),
    clienteDni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    clienteNombre: ['', Validators.maxLength(120)],
    medioPago: this.fb.control<MedioPago>('EFECTIVO'),
    observacion: ['', Validators.maxLength(255)],
    items: this.fb.array([this.nuevaLinea()]),
  });

  ngOnInit(): void {
    this.cargarProductos();
    this.almacenService.listar().subscribe((data) => {
      this.almacenes.set(data);
      if (data.length && this.form.controls.almacenId.value === null) {
        this.form.controls.almacenId.setValue(data[0].id);
      }
    });
  }

  protected get items() {
    return this.form.controls.items;
  }

  protected agregarLinea(): void {
    this.items.push(this.nuevaLinea());
  }

  protected quitarLinea(i: number): void {
    if (this.items.length > 1) {
      this.items.removeAt(i);
    }
  }

  protected producto(i: number): Producto | undefined {
    const id = Number(this.items.at(i).controls.productoId.value);
    return this.productos().find((p) => p.id === id);
  }

  protected disponible(i: number): number {
    return stockEn(this.producto(i), this.form.controls.almacenId.value);
  }

  protected excede(i: number): boolean {
    return !!this.producto(i) && Number(this.items.at(i).controls.cantidad.value) > this.disponible(i);
  }

  protected subtotal(i: number): number {
    return (this.producto(i)?.precio ?? 0) * (Number(this.items.at(i).controls.cantidad.value) || 0);
  }

  protected total(): number {
    return this.items.controls.reduce((suma, _, i) => suma + this.subtotal(i), 0);
  }

  protected hayExcesos(): boolean {
    return this.items.controls.some((_, i) => this.excede(i));
  }

  protected registrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Revise los datos: hay campos obligatorios sin completar o con formato incorrecto.');
      return;
    }
    if (this.hayExcesos()) {
      this.error.set('Hay productos con cantidad mayor al stock disponible en el almacén seleccionado.');
      return;
    }
    const v = this.form.getRawValue();
    const venta: VentaRequest = {
      almacenId: v.almacenId!,
      tipoComprobante: v.tipoComprobante,
      clienteDni: v.clienteDni,
      clienteNombre: v.clienteNombre || null,
      medioPago: v.medioPago,
      observacion: v.observacion || null,
      items: v.items.map((it) => ({ productoId: Number(it.productoId), cantidad: Number(it.cantidad) })),
    };
    this.guardando.set(true);
    this.error.set(null);
    this.movimientoService.registrarVenta(venta).subscribe({
      next: (mov) => {
        this.registrada.set(mov);
        this.guardando.set(false);
        this.limpiar();
        this.cargarProductos();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(mensajeError(err));
      },
    });
  }

  private cargarProductos(): void {
    this.productoService.listar().subscribe((data) => this.productos.set(data));
  }

  private limpiar(): void {
    const { almacenId, tipoComprobante, medioPago } = this.form.getRawValue();
    this.form.reset({ almacenId, tipoComprobante, medioPago });
    this.items.clear();
    this.items.push(this.nuevaLinea());
  }

  private nuevaLinea() {
    return this.fb.group({
      productoId: this.fb.control<number | null>(null, Validators.required),
      cantidad: [1, [Validators.required, Validators.min(1)]],
    });
  }
}
