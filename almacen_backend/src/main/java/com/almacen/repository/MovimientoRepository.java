package com.almacen.repository;

import com.almacen.entity.Movimiento;
import com.almacen.entity.TipoMovimiento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    /** Busca movimientos con filtros opcionales (los parámetros null se ignoran). */
    @EntityGraph(attributePaths = {"almacenOrigen", "almacenDestino", "detalles", "detalles.producto"})
    @Query("""
            select distinct m from Movimiento m
            where (:tipo is null or m.tipo = :tipo)
              and (:almacenId is null or m.almacenOrigen.id = :almacenId or m.almacenDestino.id = :almacenId)
              and (:desde is null or m.fecha >= :desde)
              and (:hasta is null or m.fecha < :hasta)
            order by m.fecha desc, m.id desc
            """)
    List<Movimiento> buscar(TipoMovimiento tipo, Long almacenId, LocalDateTime desde, LocalDateTime hasta);

    boolean existsByDetallesProductoId(Long productoId);
}
