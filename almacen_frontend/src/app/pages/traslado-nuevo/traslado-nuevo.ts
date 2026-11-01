import { DecimalPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { mensajeError } from '../../core/errores';
import { Almacen } from '../../models/almacen';
import { Movimiento, TrasladoRequest } from '../../models/movimiento';
import { Producto, stockEn } from '../../models/producto';
import { AlmacenService } from '../../services/almacen';
import { MovimientoService } from '../../services/movimiento';
import { ProductoService } from '../../services/producto';

/** Traslado interno entre los almacenes de la empresa. */
@Component({
  imports: [ReactiveFormsModule, DecimalPipe, RouterLink],
  selector: 'app-traslado-nuevo',
  templateUrl: './traslado-nuevo.html',
})
export class TrasladoNuevo implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly productoService = inject(ProductoService);
  private readonly almacenService = inject(AlmacenService);
  private readonly movimientoService = inject(MovimientoService);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly almacenes = signal<Almacen[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly registrado = signal<Movimiento | null>(null);
  protected readonly guardando = signal(false);

  protected readonly form = this.fb.group({
    almacenOrigenId: this.fb.control<number | null>(null, Validators.required),
    almacenDestinoId: this.fb.control<number | null>(null, Validators.required),
    observacion: ['', Validators.maxLength(255)],
    items: this.fb.array([this.nuevaLinea()]),
  });

  ngOnInit(): void {
    this.cargarProductos();
    this.almacenService.listar().subscribe((data) => {
      this.almacenes.set(data);
      if (data.length >= 2 && this.form.controls.almacenOrigenId.value === null) {
        this.form.patchValue({ almacenOrigenId: data[0].id, almacenDestinoId: data[1].id });
      }
    });
  }

  protected get items() {
    return this.form.controls.items;
  }

  protected mismoAlmacen(): boolean {
    const { almacenOrigenId, almacenDestinoId } = this.form.getRawValue();
    return almacenOrigenId !== null && almacenOrigenId === almacenDestinoId;
  }

  /** Intercambia origen y destino. */
  protected invertir(): void {
    const { almacenOrigenId, almacenDestinoId } = this.form.getRawValue();
    this.form.patchValue({ almacenOrigenId: almacenDestinoId, almacenDestinoId: almacenOrigenId });
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

  protected enOrigen(i: number): number {
    return stockEn(this.producto(i), this.form.controls.almacenOrigenId.value);
  }

  protected enDestino(i: number): number {
    return stockEn(this.producto(i), this.form.controls.almacenDestinoId.value);
  }

  protected excede(i: number): boolean {
    return !!this.producto(i) && Number(this.items.at(i).controls.cantidad.value) > this.enOrigen(i);
  }

  protected registrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Revise los datos: hay campos obligatorios sin completar.');
      return;
    }
    if (this.mismoAlmacen()) {
      this.error.set('El almacén de origen y el de destino deben ser distintos.');
      return;
    }
    if (this.items.controls.some((_, i) => this.excede(i))) {
      this.error.set('Hay productos con cantidad mayor al stock del almacén de origen.');
      return;
    }
    const v = this.form.getRawValue();
    const traslado: TrasladoRequest = {
      almacenOrigenId: v.almacenOrigenId!,
      almacenDestinoId: v.almacenDestinoId!,
      observacion: v.observacion || null,
      items: v.items.map((it) => ({ productoId: Number(it.productoId), cantidad: Number(it.cantidad) })),
    };
    this.guardando.set(true);
    this.error.set(null);
    this.movimientoService.registrarTraslado(traslado).subscribe({
      next: (mov) => {
        this.registrado.set(mov);
        this.guardando.set(false);
        this.form.controls.observacion.reset();
        this.items.clear();
        this.items.push(this.nuevaLinea());
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

  private nuevaLinea() {
    return this.fb.group({
      productoId: this.fb.control<number | null>(null, Validators.required),
      cantidad: [1, [Validators.required, Validators.min(1)]],
    });
  }
}
