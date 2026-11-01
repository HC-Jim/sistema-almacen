package com.almacen.service;

import com.almacen.dto.CompraRequest;
import com.almacen.dto.ItemCantidad;
import com.almacen.dto.MovimientoResponse;
import com.almacen.dto.TrasladoRequest;
import com.almacen.dto.VentaRequest;
import com.almacen.entity.Almacen;
import com.almacen.entity.Movimiento;
import com.almacen.entity.MovimientoDetalle;
import com.almacen.entity.Producto;
import com.almacen.entity.Stock;
import com.almacen.entity.TipoMovimiento;
import com.almacen.exception.RecursoNoEncontradoException;
import com.almacen.exception.ReglaNegocioException;
import com.almacen.repository.AlmacenRepository;
import com.almacen.repository.MovimientoRepository;
import com.almacen.repository.ProductoRepository;
import com.almacen.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Registra los movimientos de inventario. Cada operación es una transacción:
 * si un producto falla (por ejemplo, stock insuficiente) no se guarda nada.
 */
@Service
@RequiredArgsConstructor
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final AlmacenRepository almacenRepository;
    private final StockRepository stockRepository;
    private final MovimientoRepository movimientoRepository;
    private final CorrelativoService correlativoService;

    // ------------------------------------------------------------------ Compras

    /**
     * Ingreso por compra: suma stock al almacén destino y recalcula el costo promedio ponderado:
     * nuevo costo = (stock actual × costo actual + cantidad × costo de compra) / (stock actual + cantidad).
     */
    @Transactional
    public MovimientoResponse registrarCompra(CompraRequest request, String usuario) {
        validarSinRepetidos(request.items(), CompraRequest.Item::productoId);
        Almacen destino = buscarAlmacen(request.almacenId());

        Movimiento movimiento = nuevoMovimiento(TipoMovimiento.COMPRA, CorrelativoService.COMPRA, usuario,
                request.observacion());
        movimiento.setAlmacenDestino(destino);
        movimiento.setOrdenCompra(request.ordenCompra().trim());
        movimiento.setTipoCompra(request.tipoCompra());
        movimiento.setProveedor(vacioANull(request.proveedor()));

        BigDecimal total = BigDecimal.ZERO;
        for (CompraRequest.Item item : ordenar(request.items(), CompraRequest.Item::productoId)) {
            Producto producto = bloquearProducto(item.productoId());
            int stockAnterior = producto.getStock();
            int cantidad = item.cantidad();
            BigDecimal costoCompra = item.costoUnitario();

            BigDecimal nuevoCosto = stockAnterior <= 0
                    ? costoCompra
                    : producto.costoPromedioOCero().multiply(BigDecimal.valueOf(stockAnterior))
                    .add(costoCompra.multiply(BigDecimal.valueOf(cantidad)))
                    .divide(BigDecimal.valueOf((long) stockAnterior + cantidad), 4, RoundingMode.HALF_UP);

            producto.setCostoPromedio(nuevoCosto.setScale(4, RoundingMode.HALF_UP));
            producto.setUltimoCosto(costoCompra);
            producto.setStock(stockAnterior + cantidad);

            Stock stock = bloquearStock(destino, producto);
            stock.setCantidad(stock.getCantidad() + cantidad);

            BigDecimal subtotal = costoCompra.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
            movimiento.agregarDetalle(detalle(producto, cantidad, costoCompra, null, subtotal));
            total = total.add(subtotal);
        }
        movimiento.setTotal(total);
        return MovimientoResponse.from(movimientoRepository.save(movimiento));
    }

    // ------------------------------------------------------------------- Ventas

    /** Egreso por venta: descuenta stock del almacén y genera el número de comprobante. */
    @Transactional
    public MovimientoResponse registrarVenta(VentaRequest request, String usuario) {
        validarSinRepetidos(request.items(), ItemCantidad::productoId);
        Almacen origen = buscarAlmacen(request.almacenId());

        Movimiento movimiento = nuevoMovimiento(TipoMovimiento.VENTA, CorrelativoService.VENTA, usuario,
                request.observacion());
        movimiento.setAlmacenOrigen(origen);
        movimiento.setTipoComprobante(request.tipoComprobante());
        movimiento.setNumeroComprobante(correlativoService.siguiente(request.tipoComprobante().name()));
        movimiento.setClienteDni(request.clienteDni());
        movimiento.setClienteNombre(vacioANull(request.clienteNombre()));
        movimiento.setMedioPago(request.medioPago());

        BigDecimal total = BigDecimal.ZERO;
        for (ItemCantidad item : ordenar(request.items(), ItemCantidad::productoId)) {
            Producto producto = bloquearProducto(item.productoId());
            if (producto.getPrecio() == null || producto.getPrecio().signum() <= 0) {
                throw new ReglaNegocioException("El producto " + producto.getCodigo() + " no tiene precio de venta");
            }
            descontarStock(origen, producto, item.cantidad());

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.cantidad()))
                    .setScale(2, RoundingMode.HALF_UP);
            movimiento.agregarDetalle(detalle(producto, item.cantidad(), producto.costoPromedioOCero(),
                    producto.getPrecio(), subtotal));
            total = total.add(subtotal);
        }
        movimiento.setTotal(total);
        return MovimientoResponse.from(movimientoRepository.save(movimiento));
    }

    // ---------------------------------------------------------------- Traslados

    /** Traslado interno: sale del almacén de origen y entra al de destino. El stock total no cambia. */
    @Transactional
    public MovimientoResponse registrarTraslado(TrasladoRequest request, String usuario) {
        if (request.almacenOrigenId().equals(request.almacenDestinoId())) {
            throw new ReglaNegocioException("El almacén de origen y el de destino deben ser distintos");
        }
        validarSinRepetidos(request.items(), ItemCantidad::productoId);
        Almacen origen = buscarAlmacen(request.almacenOrigenId());
        Almacen destino = buscarAlmacen(request.almacenDestinoId());

        Movimiento movimiento = nuevoMovimiento(TipoMovimiento.TRASLADO, CorrelativoService.TRASLADO, usuario,
                request.observacion());
        movimiento.setAlmacenOrigen(origen);
        movimiento.setAlmacenDestino(destino);

        BigDecimal total = BigDecimal.ZERO;
        for (ItemCantidad item : ordenar(request.items(), ItemCantidad::productoId)) {
            Producto producto = bloquearProducto(item.productoId());
            descontarStock(origen, producto, item.cantidad());
            producto.setStock(producto.getStock() + item.cantidad()); // descontarStock restó del total; aquí se repone
            Stock stockDestino = bloquearStock(destino, producto);
            stockDestino.setCantidad(stockDestino.getCantidad() + item.cantidad());

            BigDecimal costo = producto.costoPromedioOCero();
            BigDecimal subtotal = costo.multiply(BigDecimal.valueOf(item.cantidad())).setScale(2, RoundingMode.HALF_UP);
            movimiento.agregarDetalle(detalle(producto, item.cantidad(), costo, null, subtotal));
            total = total.add(subtotal);
        }
        movimiento.setTotal(total);
        return MovimientoResponse.from(movimientoRepository.save(movimiento));
    }

    // ---------------------------------------------------------------- Consultas

    @Transactional(readOnly = true)
    public List<MovimientoResponse> listar(TipoMovimiento tipo, Long almacenId, LocalDate desde, LocalDate hasta) {
        return movimientoRepository.buscar(tipo, almacenId,
                        desde == null ? null : desde.atStartOfDay(),
                        hasta == null ? null : hasta.plusDays(1).atStartOfDay())
                .stream()
                .map(MovimientoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovimientoResponse obtener(Long id) {
        return movimientoRepository.findById(id)
                .map(MovimientoResponse::from)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el movimiento con id " + id));
    }

    // ------------------------------------------------------------------ Ayudas

    private Movimiento nuevoMovimiento(TipoMovimiento tipo, String claveCorrelativo, String usuario, String observacion) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipo(tipo);
        movimiento.setCorrelativo(correlativoService.siguiente(claveCorrelativo));
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setUsuario(usuario);
        movimiento.setObservacion(vacioANull(observacion));
        return movimiento;
    }

    private void descontarStock(Almacen almacen, Producto producto, int cantidad) {
        Stock stock = bloquearStock(almacen, producto);
        if (stock.getCantidad() < cantidad) {
            throw new ReglaNegocioException("Stock insuficiente de " + producto.getCodigo() + " en "
                    + almacen.getNombre() + ": disponible " + stock.getCantidad() + ", solicitado " + cantidad);
        }
        stock.setCantidad(stock.getCantidad() - cantidad);
        producto.setStock(producto.getStock() - cantidad);
    }

    private Producto bloquearProducto(Long id) {
        return productoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con id " + id));
    }

    private Stock bloquearStock(Almacen almacen, Producto producto) {
        return stockRepository.findForUpdate(almacen.getId(), producto.getId())
                .orElseGet(() -> stockRepository.save(new Stock(almacen, producto)));
    }

    private Almacen buscarAlmacen(Long id) {
        return almacenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el almacén con id " + id));
    }

    private static MovimientoDetalle detalle(Producto producto, int cantidad, BigDecimal costo,
                                             BigDecimal precio, BigDecimal subtotal) {
        MovimientoDetalle detalle = new MovimientoDetalle();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setCostoUnitario(costo);
        detalle.setPrecioUnitario(precio);
        detalle.setSubtotal(subtotal);
        return detalle;
    }

    /** Ordena por producto para bloquear las filas siempre en el mismo orden (evita bloqueos cruzados). */
    private static <T> List<T> ordenar(List<T> items, Function<T, Long> productoId) {
        return items.stream().sorted(Comparator.comparing(productoId)).toList();
    }

    private static <T> void validarSinRepetidos(List<T> items, Function<T, Long> productoId) {
        Set<Long> vistos = new HashSet<>();
        for (T item : items) {
            if (!vistos.add(productoId.apply(item))) {
                throw new ReglaNegocioException("Un producto está repetido en el detalle; junte las cantidades en una sola línea");
            }
        }
    }

    private static String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
