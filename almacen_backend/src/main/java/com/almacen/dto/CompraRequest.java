package com.almacen.dto;

import com.almacen.entity.TipoCompra;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** Ingreso de mercadería por compra. */
public record CompraRequest(
        @NotNull(message = "Seleccione el almacén de destino")
        Long almacenId,

        @NotBlank(message = "La orden de compra es obligatoria")
        @Size(max = 30, message = "La orden de compra admite máximo 30 caracteres")
        String ordenCompra,

        @NotNull(message = "Indique si la compra es nacional o importada")
        TipoCompra tipoCompra,

        @Size(max = 120, message = "El proveedor admite máximo 120 caracteres")
        String proveedor,

        @Size(max = 255, message = "La observación admite máximo 255 caracteres")
        String observacion,

        @NotEmpty(message = "Agregue al menos un producto")
        List<@Valid Item> items
) {
    public record Item(
            @NotNull(message = "Seleccione el producto")
            Long productoId,

            @NotNull(message = "Indique la cantidad")
            @Min(value = 1, message = "La cantidad debe ser al menos 1")
            Integer cantidad,

            @NotNull(message = "Indique el costo unitario")
            @DecimalMin(value = "0.0001", message = "El costo debe ser mayor a cero")
            BigDecimal costoUnitario
    ) {
    }
}
