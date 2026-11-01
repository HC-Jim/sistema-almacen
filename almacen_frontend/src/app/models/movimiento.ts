import { Almacen } from './almacen';

export type TipoMovimiento = 'COMPRA' | 'VENTA' | 'TRASLADO';
export type TipoCompra = 'NACIONAL' | 'IMPORTADA';
export type TipoComprobante = 'FACTURA' | 'BOLETA' | 'TICKET';
export type MedioPago = 'EFECTIVO' | 'TARJETA' | 'YAPE' | 'PLIN' | 'TRANSFERENCIA';

export const TIPOS_COMPROBANTE: TipoComprobante[] = ['BOLETA', 'FACTURA', 'TICKET'];
export const MEDIOS_PAGO: MedioPago[] = ['EFECTIVO', 'TARJETA', 'YAPE', 'PLIN', 'TRANSFERENCIA'];

export interface MovimientoDetalle {
  productoId: number;
  productoCodigo: string;
  productoNombre: string;
  cantidad: number;
  costoUnitario: number;
  precioUnitario: number | null;
  subtotal: number;
}

export interface Movimiento {
  id: number;
  tipo: TipoMovimiento;
  correlativo: string;
  fecha: string;
  almacenOrigen: Almacen | null;
  almacenDestino: Almacen | null;
  usuario: string;
  observacion: string | null;
  ordenCompra: string | null;
  tipoCompra: TipoCompra | null;
  proveedor: string | null;
  tipoComprobante: TipoComprobante | null;
  numeroComprobante: string | null;
  clienteDni: string | null;
  clienteNombre: string | null;
  medioPago: MedioPago | null;
  total: number;
  detalles: MovimientoDetalle[];
}

export interface ItemCantidad {
  productoId: number;
  cantidad: number;
}

export interface CompraRequest {
  almacenId: number;
  ordenCompra: string;
  tipoCompra: TipoCompra;
  proveedor: string | null;
  observacion: string | null;
  items: (ItemCantidad & { costoUnitario: number })[];
}

export interface VentaRequest {
  almacenId: number;
  tipoComprobante: TipoComprobante;
  clienteDni: string;
  clienteNombre: string | null;
  medioPago: MedioPago;
  observacion: string | null;
  items: ItemCantidad[];
}

export interface TrasladoRequest {
  almacenOrigenId: number;
  almacenDestinoId: number;
  observacion: string | null;
  items: ItemCantidad[];
}

export interface FiltroMovimientos {
  tipo?: TipoMovimiento | null;
  almacenId?: number | null;
  desde?: string | null;
  hasta?: string | null;
}
