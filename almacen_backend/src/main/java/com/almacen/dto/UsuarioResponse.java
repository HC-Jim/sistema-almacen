package com.almacen.dto;

import com.almacen.entity.Usuario;

/** Usuario tal como se muestra al administrador (nunca incluye la contraseña). */
public record UsuarioResponse(Long id, String username, String nombre, String rol, boolean activo) {

    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getUsername(), u.getNombre(), u.getRol(), u.isActivo());
    }
}
