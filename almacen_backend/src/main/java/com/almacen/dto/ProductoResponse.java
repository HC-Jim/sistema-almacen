package com.almacen.dto;

import com.almacen.entity.Producto;

import java.math.BigDecimal;
import java.util.List;

/** Lo que la API devuelve al frontend (nunca la entidad JPA directamente). */
public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        BigDecimal precio,
        BigDecimal costoPromedio,
        BigDecimal ultimoCosto,
        Integer stock,
        List<StockAlmacenResponse> stockPorAlmacen
) {
    public static ProductoResponse from(Producto p, List<StockAlmacenResponse> stockPorAlmacen) {
        return new ProductoResponse(p.getId(), p.getCodigo(), p.getNombre(), p.getDescripcion(),
                p.getPrecio(), p.costoPromedioOCero(), p.getUltimoCosto(), p.getStock(), stockPorAlmacen);
    }
}
