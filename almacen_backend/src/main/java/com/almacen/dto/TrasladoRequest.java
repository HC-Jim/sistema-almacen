package com.almacen.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Traslado interno de mercadería entre dos almacenes. */
public record TrasladoRequest(
        @NotNull(message = "Seleccione el almacén de origen")
        Long almacenOrigenId,

        @NotNull(message = "Seleccione el almacén de destino")
        Long almacenDestinoId,

        @Size(max = 255, message = "La observación admite máximo 255 caracteres")
        String observacion,

        @NotEmpty(message = "Agregue al menos un producto")
        List<@Valid ItemCantidad> items
) {
}
