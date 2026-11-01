import { DecimalPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { mensajeError } from '../../core/errores';
import { Almacen } from '../../models/almacen';
import { Producto, ProductoRequest, stockEn } from '../../models/producto';
import { AlmacenService } from '../../services/almacen';
import { ProductoService } from '../../services/producto';

@Component({
  imports: [ReactiveFormsModule, DecimalPipe],
  selector: 'app-productos',
  templateUrl: './productos.html',
})
export class Productos implements OnInit {
  private readonly productoService = inject(ProductoService);
  private readonly almacenService = inject(AlmacenService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly almacenes = signal<Almacen[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);
  protected readonly editandoId = signal<number | null>(null);

  protected readonly stockEn = stockEn;

  protected readonly form = this.fb.group({
    codigo: ['', [Validators.required, Validators.maxLength(30)]],
    nombre: ['', [Validators.required, Validators.maxLength(120)]],
    descripcion: ['', Validators.maxLength(255)],
    precio: [0, [Validators.required, Validators.min(0)]],
  });

  ngOnInit(): void {
    this.almacenService.listar().subscribe((data) => this.almacenes.set(data));
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.productoService.listar().subscribe({
      next: (data) => {
        this.productos.set(data);
        this.cargando.set(false);
      },
      error: (err) => this.mostrarError(err),
    });
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const valor = this.form.getRawValue();
    const datos: ProductoRequest = { ...valor, descripcion: valor.descripcion || null };
    const id = this.editandoId();
    const peticion = id === null ? this.productoService.crear(datos) : this.productoService.actualizar(id, datos);

    this.error.set(null);
    peticion.subscribe({
      next: (p) => {
        this.exito.set(`Producto ${p.codigo} ${id === null ? 'registrado' : 'actualizado'}.`);
        this.cancelar();
        this.cargar();
      },
      error: (err) => this.mostrarError(err),
    });
  }

  protected editar(producto: Producto): void {
    this.exito.set(null);
    this.editandoId.set(producto.id);
    this.form.setValue({
      codigo: producto.codigo,
      nombre: producto.nombre,
      descripcion: producto.descripcion ?? '',
      precio: producto.precio,
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  protected eliminar(producto: Producto): void {
    if (!confirm(`¿Eliminar el producto "${producto.nombre}"?`)) {
      return;
    }
    this.productoService.eliminar(producto.id).subscribe({
      next: () => {
        this.exito.set(`Producto ${producto.codigo} eliminado.`);
        this.cargar();
      },
      error: (err) => this.mostrarError(err),
    });
  }

  protected cancelar(): void {
    this.editandoId.set(null);
    this.form.reset();
  }

  private mostrarError(err: unknown): void {
    this.cargando.set(false);
    this.exito.set(null);
    this.error.set(mensajeError(err));
  }
}
