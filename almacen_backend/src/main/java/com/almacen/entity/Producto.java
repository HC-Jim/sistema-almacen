package com.almacen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    /** Precio de venta: lo define el usuario y se puede modificar. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /** Costo promedio ponderado: solo lo recalculan las compras. */
    @Column(name = "costo_promedio", precision = 14, scale = 4)
    private BigDecimal costoPromedio;

    /** Costo unitario de la última compra registrada. */
    @Column(name = "ultimo_costo", precision = 14, scale = 4)
    private BigDecimal ultimoCosto;

    /** Stock total sumando todos los almacenes (se mantiene con cada movimiento). */
    @Column(nullable = false)
    private Integer stock;

    public BigDecimal costoPromedioOCero() {
        return costoPromedio == null ? BigDecimal.ZERO : costoPromedio;
    }
}
