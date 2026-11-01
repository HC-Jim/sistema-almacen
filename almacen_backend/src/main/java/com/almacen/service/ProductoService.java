package com.almacen.service;

import com.almacen.dto.PrecioRequest;
import com.almacen.dto.ProductoRequest;
import com.almacen.dto.ProductoResponse;
import com.almacen.dto.StockAlmacenResponse;
import com.almacen.entity.Producto;
import com.almacen.entity.Stock;
import com.almacen.exception.RecursoNoEncontradoException;
import com.almacen.exception.ReglaNegocioException;
import com.almacen.repository.MovimientoRepository;
import com.almacen.repository.ProductoRepository;
import com.almacen.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final StockRepository stockRepository;
    private final MovimientoRepository movimientoRepository;

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        Map<Long, List<StockAlmacenResponse>> stockPorProducto = stockPorProducto();
        return productoRepository.findAllByOrderByCodigoAsc().stream()
                .map(p -> ProductoResponse.from(p, stockPorProducto.getOrDefault(p.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        Producto producto = buscar(id);
        return ProductoResponse.from(producto, stockPorProducto().getOrDefault(id, List.of()));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        if (productoRepository.existsByCodigo(request.codigo().trim())) {
            throw new ReglaNegocioException("Ya existe un producto con el código " + request.codigo());
        }
        Producto producto = new Producto();
        copiar(request, producto);
        producto.setStock(0);
        return ProductoResponse.from(productoRepository.save(producto), List.of());
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscar(id);
        if (productoRepository.existsByCodigoAndIdNot(request.codigo().trim(), id)) {
            throw new ReglaNegocioException("Ya existe otro producto con el código " + request.codigo());
        }
        copiar(request, producto);
        return obtener(productoRepository.save(producto).getId());
    }

    /** Solo cambia el precio de venta; el costo no se toca. */
    @Transactional
    public ProductoResponse actualizarPrecio(Long id, PrecioRequest request) {
        Producto producto = buscar(id);
        producto.setPrecio(request.precio());
        return obtener(productoRepository.save(producto).getId());
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscar(id);
        if (movimientoRepository.existsByDetallesProductoId(id) || stockRepository.existsByProductoId(id)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar " + producto.getCodigo() + " porque tiene movimientos o stock registrado");
        }
        productoRepository.delete(producto);
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con id " + id));
    }

    private void copiar(ProductoRequest request, Producto producto) {
        producto.setCodigo(request.codigo().trim());
        producto.setNombre(request.nombre().trim());
        producto.setDescripcion(request.descripcion());
        producto.setPrecio(request.precio());
    }

    private Map<Long, List<StockAlmacenResponse>> stockPorProducto() {
        return stockRepository.findAllConDetalle().stream()
                .sorted(Comparator.comparing((Stock s) -> s.getAlmacen().getCodigo()))
                .collect(Collectors.groupingBy(s -> s.getProducto().getId(),
                        Collectors.mapping(s -> new StockAlmacenResponse(
                                s.getAlmacen().getId(), s.getAlmacen().getCodigo(), s.getCantidad()),
                                Collectors.toList())));
    }
}
