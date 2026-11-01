package com.almacen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lleva el último número usado de cada serie (COMPRA, VENTA, TRASLADO, FACTURA, BOLETA, TICKET).
 * Se lee con bloqueo para que dos operaciones simultáneas no repitan número.
 */
@Entity
@Table(name = "correlativos")
@Getter
@Setter
@NoArgsConstructor
public class Correlativo {

    @Id
    @Column(length = 20)
    private String clave;

    @Column(nullable = false, length = 10)
    private String prefijo;

    @Column(nullable = false)
    private Integer digitos;

    @Column(nullable = false)
    private Long ultimo;

    public Correlativo(String clave, String prefijo, int digitos) {
        this.clave = clave;
        this.prefijo = prefijo;
        this.digitos = digitos;
        this.ultimo = 0L;
    }
}
