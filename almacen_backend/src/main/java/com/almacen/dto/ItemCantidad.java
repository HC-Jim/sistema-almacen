package com.almacen.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Producto y cantidad (usado en ventas y traslados). */
public record ItemCantidad(
        @NotNull(message = "Seleccione el producto")
        Long productoId,

        @NotNull(message = "Indique la cantidad")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        Integer cantidad
) {
}
