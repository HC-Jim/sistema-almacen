package com.almacen.dto;

import com.almacen.entity.Almacen;

public record AlmacenResponse(Long id, String codigo, String nombre, String direccion) {

    public static AlmacenResponse from(Almacen a) {
        return new AlmacenResponse(a.getId(), a.getCodigo(), a.getNombre(), a.getDireccion());
    }
}
