import { DecimalPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { mensajeError } from '../../core/errores';
import { Producto } from '../../models/producto';
import { ProductoService } from '../../services/producto';

/** Precios de venta: se pueden modificar. El costo se muestra solo como referencia. */
@Component({
  imports: [FormsModule, DecimalPipe],
  selector: 'app-precios',
  templateUrl: './precios.html',
})
export class Precios implements OnInit {
  private readonly productoService = inject(ProductoService);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);
  protected readonly guardandoId = signal<number | null>(null);
  protected filtro = '';

  /** Nuevo precio escrito por el usuario, por id de producto. */
  protected nuevos: Record<number, number | null> = {};

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.productoService.listar().subscribe({
      next: (data) => {
        this.productos.set(data);
        this.nuevos = Object.fromEntries(data.map((p) => [p.id, p.precio]));
      },
      error: (err) => this.error.set(mensajeError(err)),
    });
  }

  protected visibles(): Producto[] {
    const texto = this.filtro.trim().toLowerCase();
    return texto
      ? this.productos().filter((p) => `${p.codigo} ${p.nombre}`.toLowerCase().includes(texto))
      : this.productos();
  }

  /** Margen sobre el precio: (precio - costo) / precio. */
  protected margen(precio: number | null, costo: number): number | null {
    return precio && precio > 0 && costo > 0 ? ((precio - costo) / precio) * 100 : null;
  }

  protected cambiado(p: Producto): boolean {
    const nuevo = this.nuevos[p.id];
    return nuevo !== null && nuevo !== undefined && Number(nuevo) !== Number(p.precio);
  }

  protected guardar(p: Producto): void {
    const nuevo = Number(this.nuevos[p.id]);
    if (!nuevo || nuevo <= 0) {
      this.error.set(`Ingrese un precio válido para ${p.codigo}.`);
      return;
    }
    if (p.costoPromedio > 0 && nuevo < p.costoPromedio) {
      const ok = confirm(
        `El precio S/ ${nuevo.toFixed(2)} es MENOR al costo S/ ${p.costoPromedio.toFixed(2)}.\n` +
          'Venderá con pérdida. ¿Desea guardarlo de todas formas?',
      );
      if (!ok) {
        return;
      }
    }
    this.guardandoId.set(p.id);
    this.error.set(null);
    this.productoService.actualizarPrecio(p.id, nuevo).subscribe({
      next: (actualizado) => {
        this.productos.update((lista) => lista.map((x) => (x.id === actualizado.id ? actualizado : x)));
        this.exito.set(`Precio de ${actualizado.codigo} actualizado a S/ ${actualizado.precio.toFixed(2)}.`);
        this.guardandoId.set(null);
      },
      error: (err) => {
        this.guardandoId.set(null);
        this.error.set(mensajeError(err));
      },
    });
  }
}
