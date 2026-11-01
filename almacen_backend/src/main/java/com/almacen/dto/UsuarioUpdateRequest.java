package com.almacen.dto;

import com.almacen.config.Roles;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos editables de un usuario existente (el usuario y la contraseña no se cambian aquí). */
public record UsuarioUpdateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre admite como máximo 100 caracteres")
        String nombre,

        @NotBlank(message = "El rol es obligatorio")
        @Pattern(regexp = Roles.PATRON, message = "El rol debe ser ADMIN, COMERCIAL, COMPRAS o ALMACEN")
        String rol,

        @NotNull(message = "Indique si el usuario está activo")
        Boolean activo
) {
}
