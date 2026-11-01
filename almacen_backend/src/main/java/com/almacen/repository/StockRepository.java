package com.almacen.repository;

import com.almacen.entity.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.almacen.id = :almacenId and s.producto.id = :productoId")
    Optional<Stock> findForUpdate(Long almacenId, Long productoId);

    @Query("select s from Stock s join fetch s.almacen join fetch s.producto")
    List<Stock> findAllConDetalle();

    boolean existsByProductoId(Long productoId);
}
