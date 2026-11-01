import { DecimalPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { mensajeError } from '../../core/errores';
import { Almacen } from '../../models/almacen';
import { CompraRequest, Movimiento } from '../../models/movimiento';
import { Producto } from '../../models/producto';
import { AlmacenService } from '../../services/almacen';
import { MovimientoService } from '../../services/movimiento';
import { ProductoService } from '../../services/producto';

/** Ingreso por compra: orden de compra, tipo (nacional/importada) y productos con su costo. */
@Component({
  imports: [ReactiveFormsModule, DecimalPipe, RouterLink],
  selector: 'app-compra-nueva',
  templateUrl: './compra-nueva.html',
})
export class CompraNueva implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly productoService = inject(ProductoService);
  private readonly almacenService = inject(AlmacenService);
  private readonly movimientoService = inject(MovimientoService);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly almacenes = signal<Almacen[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly registrada = signal<Movimiento | null>(null);
  protected readonly guardando = signal(false);

  protected readonly form = this.fb.group({
    almacenId: this.fb.control<number | null>(null, Validators.required),
    ordenCompra: ['', [Validators.required, Validators.maxLength(30)]],
    tipoCompra: this.fb.control<'NACIONAL' | 'IMPORTADA'>('NACIONAL'),
    proveedor: ['', Validators.maxLength(120)],
    observacion: ['', Validators.maxLength(255)],
    items: this.fb.array([this.nuevaLinea()]),
  });

  ngOnInit(): void {
    this.productoService.listar().subscribe((data) => this.productos.set(data));
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

  /** Al elegir un producto, propone como costo el de su última compra. */
  protected alElegirProducto(i: number): void {
    const linea = this.items.at(i);
    const producto = this.productos().find((p) => p.id === Number(linea.controls.productoId.value));
    if (producto?.ultimoCosto && !linea.controls.costoUnitario.value) {
      linea.controls.costoUnitario.setValue(producto.ultimoCosto);
    }
  }

  protected subtotal(i: number): number {
    const v = this.items.at(i).getRawValue();
    return (Number(v.cantidad) || 0) * (Number(v.costoUnitario) || 0);
  }

  protected total(): number {
    return this.items.controls.reduce((suma, _, i) => suma + this.subtotal(i), 0);
  }

  protected costoActual(i: number): number | null {
    const id = Number(this.items.at(i).controls.productoId.value);
    return this.productos().find((p) => p.id === id)?.costoPromedio ?? null;
  }

  protected registrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Revise los datos: hay campos obligatorios sin completar.');
      return;
    }
    const v = this.form.getRawValue();
    const compra: CompraRequest = {
      almacenId: v.almacenId!,
      ordenCompra: v.ordenCompra.trim(),
      tipoCompra: v.tipoCompra,
      proveedor: v.proveedor || null,
      observacion: v.observacion || null,
      items: v.items.map((it) => ({
        productoId: Number(it.productoId),
        cantidad: Number(it.cantidad),
        costoUnitario: Number(it.costoUnitario),
      })),
    };
    this.guardando.set(true);
    this.error.set(null);
    this.movimientoService.registrarCompra(compra).subscribe({
      next: (mov) => {
        this.registrada.set(mov);
        this.guardando.set(false);
        this.limpiar();
        this.productoService.listar().subscribe((data) => this.productos.set(data));
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(mensajeError(err));
      },
    });
  }

  private limpiar(): void {
    const almacenId = this.form.controls.almacenId.value;
    this.form.reset();
    this.form.controls.almacenId.setValue(almacenId);
    this.items.clear();
    this.items.push(this.nuevaLinea());
  }

  private nuevaLinea() {
    return this.fb.group({
      productoId: this.fb.control<number | null>(null, Validators.required),
      cantidad: [1, [Validators.required, Validators.min(1)]],
      costoUnitario: [0, [Validators.required, Validators.min(0.0001)]],
    });
  }
}
