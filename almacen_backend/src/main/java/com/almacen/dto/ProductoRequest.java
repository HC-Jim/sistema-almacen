package com.almacen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos editables de un producto (logística). El stock, el costo y el precio no se envían:
 * el stock cambia solo con compras, ventas y traslados, el costo solo con compras
 * y el precio lo fija el área comercial (PATCH /api/productos/{id}/precio).
 */
public record ProductoRequest(
        @NotBlank(message = "El código es obligatorio")
        @Size(max = 30, message = "El código admite máximo 30 caracteres")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre admite máximo 120 caracteres")
        String nombre,

        @Size(max = 255, message = "La descripción admite máximo 255 caracteres")
        String descripcion
) {
}
