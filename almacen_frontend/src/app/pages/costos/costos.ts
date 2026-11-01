import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { mensajeError } from '../../core/errores';
import { Producto } from '../../models/producto';
import { ProductoService } from '../../services/producto';

/** Consulta de costos (solo lectura). El costo lo recalcula el sistema con cada compra. */
@Component({
  imports: [DecimalPipe, FormsModule],
  selector: 'app-costos',
  templateUrl: './costos.html',
})
export class Costos implements OnInit {
  private readonly productoService = inject(ProductoService);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly error = signal<string | null>(null);
  protected filtro = '';

  protected readonly totalValorizado = computed(() =>
    this.productos().reduce((suma, p) => suma + p.stock * p.costoPromedio, 0),
  );

  ngOnInit(): void {
    this.productoService.listar().subscribe({
      next: (data) => this.productos.set(data),
      error: (err) => this.error.set(mensajeError(err)),
    });
  }

  protected visibles(): Producto[] {
    const texto = this.filtro.trim().toLowerCase();
    return texto
      ? this.productos().filter((p) => `${p.codigo} ${p.nombre}`.toLowerCase().includes(texto))
      : this.productos();
  }
}
