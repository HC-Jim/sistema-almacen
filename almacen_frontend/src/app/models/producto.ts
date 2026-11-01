export interface StockAlmacen {
  almacenId: number;
  almacenCodigo: string;
  cantidad: number;
}

/** Producto tal como lo devuelve el backend (ProductoResponse). */
export interface Producto {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string | null;
  /** Precio de venta (editable). */
  precio: number;
  /** Costo promedio ponderado (solo lectura, lo recalculan las compras). */
  costoPromedio: number;
  ultimoCosto: number | null;
  /** Stock total en todos los almacenes. */
  stock: number;
  stockPorAlmacen: StockAlmacen[];
}

/** Datos que se envían al crear o editar un producto (el stock y el costo no se editan). */
export interface ProductoRequest {
  codigo: string;
  nombre: string;
  descripcion: string | null;
  precio: number;
}

export function stockEn(producto: Producto | undefined, almacenId: number | null): number {
  if (!producto || almacenId === null) {
    return 0;
  }
  return producto.stockPorAlmacen.find((s) => s.almacenId === almacenId)?.cantidad ?? 0;
}
