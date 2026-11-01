package com.almacen.repository;

import com.almacen.entity.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlmacenRepository extends JpaRepository<Almacen, Long> {

    List<Almacen> findAllByOrderByCodigoAsc();

    Optional<Almacen> findByCodigo(String codigo);
}
