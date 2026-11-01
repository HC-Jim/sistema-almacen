import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { environment } from '../../environments/environment';
import { Producto, ProductoRequest } from '../models/producto';

@Service()
export class ProductoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/productos`;

  listar() {
    return this.http.get<Producto[]>(this.url);
  }

  crear(producto: ProductoRequest) {
    return this.http.post<Producto>(this.url, producto);
  }

  actualizar(id: number, producto: ProductoRequest) {
    return this.http.put<Producto>(`${this.url}/${id}`, producto);
  }

  /** Cambia solo el precio de venta. */
  actualizarPrecio(id: number, precio: number) {
    return this.http.patch<Producto>(`${this.url}/${id}/precio`, { precio });
  }

  eliminar(id: number) {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
