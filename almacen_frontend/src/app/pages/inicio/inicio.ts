import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth';
import { MODULOS } from '../../core/modulos';
import { Almacen } from '../../models/almacen';
import { Producto, stockEn } from '../../models/producto';
import { AlmacenService } from '../../services/almacen';
import { ProductoService } from '../../services/producto';

/** Menú principal: resumen del inventario y acceso a los módulos del rol del usuario. */
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

  /** Solo los módulos donde el rol del usuario tiene alguna opción. */
  protected readonly modulos = computed(() =>
    MODULOS.filter((m) => m.opciones.some((o) => this.auth.puede(o.roles))),
  );

  ngOnInit(): void {
    this.productoService.listar().subscribe((data) => this.productos.set(data));
    this.almacenService.listar().subscribe((data) => this.almacenes.set(data));
  }

  protected stockAlmacen(almacenId: number): number {
    return this.productos().reduce((suma, p) => suma + stockEn(p, almacenId), 0);
  }
}
