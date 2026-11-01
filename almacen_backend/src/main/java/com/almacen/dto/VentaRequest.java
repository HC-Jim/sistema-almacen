package com.almacen.dto;

import com.almacen.entity.MedioPago;
import com.almacen.entity.TipoComprobante;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Egreso de mercadería por venta. El precio se toma del precio de venta vigente. */
public record VentaRequest(
        @NotNull(message = "Seleccione el almacén de despacho")
        Long almacenId,

        @NotNull(message = "Seleccione el tipo de comprobante")
        TipoComprobante tipoComprobante,

        @NotNull(message = "El DNI del cliente es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
        String clienteDni,

        @Size(max = 120, message = "El nombre admite máximo 120 caracteres")
        String clienteNombre,

        @NotNull(message = "Seleccione el medio de pago")
        MedioPago medioPago,

        @Size(max = 255, message = "La observación admite máximo 255 caracteres")
        String observacion,

        @NotEmpty(message = "Agregue al menos un producto")
        List<@Valid ItemCantidad> items
) {
}
