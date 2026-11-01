package com.almacen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos para crear un usuario. */
public record UsuarioRequest(
        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 50, message = "El usuario admite como máximo 50 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "El usuario solo admite letras, números, punto, guion y guion bajo")
        String username,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre admite como máximo 100 caracteres")
        String nombre,

        @NotBlank(message = "El rol es obligatorio")
        @Pattern(regexp = "ADMIN|VENDEDOR", message = "El rol debe ser ADMIN o VENDEDOR")
        String rol,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password
) {
}
