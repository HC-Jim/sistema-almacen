package com.almacen.dto;

import com.almacen.entity.Almacen;
import com.almacen.entity.MedioPago;
import com.almacen.entity.Movimiento;
import com.almacen.entity.MovimientoDetalle;
import com.almacen.entity.TipoCompra;
import com.almacen.entity.TipoComprobante;
import com.almacen.entity.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record MovimientoResponse(
        Long id,
        TipoMovimiento tipo,
        String correlativo,
        LocalDateTime fecha,
        AlmacenResponse almacenOrigen,
        AlmacenResponse almacenDestino,
        String usuario,
        String observacion,
        String ordenCompra,
        TipoCompra tipoCompra,
        String proveedor,
        TipoComprobante tipoComprobante,
        String numeroComprobante,
        String clienteDni,
        String clienteNombre,
        MedioPago medioPago,
        BigDecimal total,
        List<Detalle> detalles
) {
    public record Detalle(
            Long productoId,
            String productoCodigo,
            String productoNombre,
            Integer cantidad,
            BigDecimal costoUnitario,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {
        static Detalle from(MovimientoDetalle d) {
            return new Detalle(d.getProducto().getId(), d.getProducto().getCodigo(), d.getProducto().getNombre(),
                    d.getCantidad(), d.getCostoUnitario(), d.getPrecioUnitario(), d.getSubtotal());
        }
    }

    public static MovimientoResponse from(Movimiento m) {
        return new MovimientoResponse(m.getId(), m.getTipo(), m.getCorrelativo(), m.getFecha(),
                almacen(m.getAlmacenOrigen()), almacen(m.getAlmacenDestino()), m.getUsuario(), m.getObservacion(),
                m.getOrdenCompra(), m.getTipoCompra(), m.getProveedor(),
                m.getTipoComprobante(), m.getNumeroComprobante(), m.getClienteDni(), m.getClienteNombre(),
                m.getMedioPago(), m.getTotal(),
                m.getDetalles().stream().map(Detalle::from).toList());
    }

    private static AlmacenResponse almacen(Almacen a) {
        return a == null ? null : AlmacenResponse.from(a);
    }
}
