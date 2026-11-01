import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { environment } from '../../environments/environment';
import {
  CompraRequest,
  FiltroMovimientos,
  Movimiento,
  TrasladoRequest,
  VentaRequest,
} from '../models/movimiento';

@Service()
export class MovimientoService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  /** Lista movimientos; los filtros vacíos se omiten. */
  listar(filtro: FiltroMovimientos) {
    let params = new HttpParams();
    for (const [clave, valor] of Object.entries(filtro)) {
      if (valor !== null && valor !== undefined && valor !== '') {
        params = params.set(clave, String(valor));
      }
    }
    return this.http.get<Movimiento[]>(`${this.api}/movimientos`, { params });
  }

  registrarCompra(compra: CompraRequest) {
    return this.http.post<Movimiento>(`${this.api}/compras`, compra);
  }

  registrarVenta(venta: VentaRequest) {
    return this.http.post<Movimiento>(`${this.api}/ventas`, venta);
  }

  registrarTraslado(traslado: TrasladoRequest) {
    return this.http.post<Movimiento>(`${this.api}/traslados`, traslado);
  }
}
