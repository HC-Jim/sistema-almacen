package com.almacen.repository;

import com.almacen.entity.Correlativo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CorrelativoRepository extends JpaRepository<Correlativo, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Correlativo c where c.clave = :clave")
    Optional<Correlativo> findForUpdate(String clave);
}
